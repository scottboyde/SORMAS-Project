package de.symeda.sormas.app.event;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.SormasApplication;
import de.symeda.sormas.app.backend.common.AdoDtoHelper.DtoGetInterface;
import de.symeda.sormas.app.backend.common.AdoDtoHelper.DtoPostInterface;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.event.EventDtoHelper;
import de.symeda.sormas.app.backend.user.User;
import de.symeda.sormas.app.rest.RetroProvider;
import de.symeda.sormas.app.util.Callback;
import de.symeda.sormas.app.util.ErrorReportingHelper;
import de.symeda.sormas.app.util.SyncCallback;
import retrofit2.Call;

/**
 * @see <a href="http://square.github.io/retrofit/">Retrofit</a>
 */
public class SyncEventsTask extends AsyncTask<Void, Void, Void> {

    /**
     * Should be set to true when the synchronization fails and reset to false as soon
     * as the last callback is called (i.e. the synchronization has been completed/cancelled).
     */
    protected boolean hasThrownError;
    private final Context context;

    public SyncEventsTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            new EventDtoHelper().pullEntities(new DtoGetInterface<EventDto>() {
                @Override
                public Call<List<EventDto>> getAll(long since) {

                    User user = ConfigProvider.getUser();
                    if (user != null) {
                        Call<List<EventDto>> all = RetroProvider.getEventFacade().getAll(user.getUuid(), since);
                        return all;
                    }
                    return null;
                }
            }, DatabaseHelper.getEventDao(), context);

            new EventDtoHelper().pushEntities(new DtoPostInterface<EventDto>() {
                @Override
                public Call<Long> postAll(List<EventDto> dtos) {
                    return RetroProvider.getEventFacade().postAll(dtos);
                }
            }, DatabaseHelper.getEventDao());
        } catch (DaoException | SQLException | IOException e) {
            hasThrownError = true;
            Log.e(getClass().getName(), "Error while synchronizing alerts", e);
            SormasApplication application = (SormasApplication) context.getApplicationContext();
            Tracker tracker = application.getDefaultTracker();
            ErrorReportingHelper.sendCaughtException(tracker, e, null, true);
        }
        return null;
    }

    public static void syncEventsWithoutCallback(Context context, final FragmentManager fragmentManager) {
        if (fragmentManager != null) {
            createSyncEventsTask(context, new SyncCallback() {
                @Override
                public void call(boolean syncFailed) {
                    if (fragmentManager.getFragments() != null) {
                        for (Fragment fragment : fragmentManager.getFragments()) {
                            if (fragment instanceof EventsListFragment) {
                                fragment.onResume();
                            }
                        }
                    }
                }
            });
        } else {
            createSyncEventsTask(context, null);
        }
    }

    public static void syncEventsWithCallback(Context context, final FragmentManager fragmentManager, final SyncCallback callback) {
        if (fragmentManager != null) {
            createSyncEventsTask(context, new SyncCallback() {
                @Override
                public void call(boolean syncFailed) {
                    if (fragmentManager.getFragments() != null) {
                        for (Fragment fragment : fragmentManager.getFragments()) {
                            if (fragment instanceof EventsListFragment) {
                                fragment.onResume();
                            }
                        }
                    }
                    callback.call(syncFailed);
                }
            });
        } else {
            createSyncEventsTask(context, callback);
        }
    }

    /**
     * Synchronizes the events, displays a progress dialog and an error message when the synchronization fails.
     * Should only be called when the user has manually triggered the synchronization.
     *
     * @param context
     * @param callback
     */
    public static void syncEventsWithProgressDialog(Context context, final SyncCallback callback) {
        final ProgressDialog progressDialog = ProgressDialog.show(context, context.getString(R.string.headline_alert_synchronization),
                context.getString(R.string.hint_alert_synchronization), true);

        createSyncEventsTask(context, new SyncCallback() {
            @Override
            public void call(boolean syncFailed) {
                progressDialog.dismiss();
                callback.call(syncFailed);
            }
        });
    }

    public static AsyncTask<Void, Void, Void> createSyncEventsTask(final Context context, final SyncCallback callback) {
        return new SyncEventsTask(context) {
            @Override
            protected void onPostExecute(Void aVoid) {
                if (this.hasThrownError) {
                    callback.call(true);
                } else {
                    SyncEventParticipantsTask.syncEventParticipants(context, callback);
                }
                this.hasThrownError = false;
            }
        }.execute();
    }

}