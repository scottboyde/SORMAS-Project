package de.symeda.sormas.app.event.edit;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;

import de.symeda.sormas.app.BR;
import de.symeda.sormas.app.BaseEditActivityFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.component.Item;
import de.symeda.sormas.app.component.OnTeboSwitchCheckedChangeListener;
import de.symeda.sormas.app.component.TeboSpinner;
import de.symeda.sormas.app.component.TeboSwitch;
import de.symeda.sormas.app.component.dialog.LocationDialog;
import de.symeda.sormas.app.component.dialog.TeboAlertDialogInterface;
import de.symeda.sormas.app.core.IEntryItemOnClickListener;
import de.symeda.sormas.app.databinding.FragmentEventEditPersonInfoLayoutBinding;
import de.symeda.sormas.app.event.EventFormNavigationCapsule;
import de.symeda.sormas.app.task.edit.TaskEditActivity;
import de.symeda.sormas.app.util.DataUtils;
import de.symeda.sormas.app.util.MemoryDatabaseHelper;

import java.util.List;

import de.symeda.sormas.api.event.EventStatus;
import de.symeda.sormas.api.person.ApproximateAgeType;
import de.symeda.sormas.api.person.OccupationType;
import de.symeda.sormas.api.person.PresentCondition;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.app.backend.common.AbstractDomainObject;
import de.symeda.sormas.app.backend.event.EventParticipant;
import de.symeda.sormas.app.backend.location.Location;

/**
 * Created by Orson on 07/02/2018.
 * <p>
 * www.technologyboard.org
 * sampson.orson@gmail.com
 * sampson.orson@technologyboard.org
 */

public class EventEditPersonsInvolvedInfoFragment extends BaseEditActivityFragment<FragmentEventEditPersonInfoLayoutBinding> {

    public static final String TAG = EventEditPersonsInvolvedInfoFragment.class.getSimpleName();

    private String recordUuid = null;
    //private EventStatus followUpStatus = null;
    private EventStatus pageStatus = null;
    private EventParticipant record;
    private OnTeboSwitchCheckedChangeListener onPresentConditionCheckedCallback;
    private IEntryItemOnClickListener onAddressLinkClickedCallback;

    private List<OccupationType> occupationTypeList;
    private List<Sex> genderList;
    private List<ApproximateAgeType> ageTypeList;
    private List<Item> dateList;
    private List<Item> monthList;
    private List<Item> yearList;

    private int mLastCheckedId = -1;

    private OccupationTypeLayoutProcessor occupationTypeLayoutProcessor;
    private PresentConditionLayoutProcessor presentConditionLayoutProcessor;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //SaveFilterStatusState(outState, followUpStatus);
        SavePageStatusState(outState, pageStatus);
        SaveRecordUuidState(outState, recordUuid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = (savedInstanceState != null)? savedInstanceState : getArguments();

        recordUuid = getRecordUuidArg(arguments);
        //followUpStatus = (EventStatus) getFilterStatusArg(arguments);
        pageStatus = (EventStatus) getPageStatusArg(arguments);
    }

    @Override
    protected String getSubHeadingTitle() {
        return getResources().getString(R.string.heading_level4_1_event_edit_person_involved_info);
    }

    @Override
    public AbstractDomainObject getData() {
        return record;
    }

    @Override
    public void onBeforeLayoutBinding(Bundle savedInstanceState) {
        record = MemoryDatabaseHelper.EVENT_PARTICIPANT.getEventParticipants(1).get(0);
        occupationTypeList = MemoryDatabaseHelper.OCCUPATION_TYPE.getOccupationTypes(20);
        genderList = MemoryDatabaseHelper.GENDER.getGenders();
        ageTypeList = MemoryDatabaseHelper.AGE_TYPE.getAgeTypes();

        dateList = DataUtils.toItems(DateHelper.getDaysInMonth(),true);
        monthList = DataUtils.getMonthItems(true);
        yearList = DataUtils.toItems(DateHelper.getYearsToNow(),true);

        //TODO: Remove, just for testing
        record.getPerson().setOccupationType(OccupationType.OTHER);

        setupCallback();
    }

    @Override
    public void onLayoutBinding(ViewStub stub, View inflated, FragmentEventEditPersonInfoLayoutBinding contentBinding) {
        //binding = DataBindingUtil.inflate(inflater, getEditLayout(), container, true);

        occupationTypeLayoutProcessor = new OccupationTypeLayoutProcessor(getContext(), contentBinding, record.getPerson());
        occupationTypeLayoutProcessor.setOnSetBindingVariable(new OnSetBindingVariableListener() {
            @Override
            public void onSetBindingVariable(ViewDataBinding binding, String layoutName) {
                setRootNotificationBindingVariable(binding, layoutName);
                setLocalBindingVariable(binding, layoutName);
            }
        });

        presentConditionLayoutProcessor = new PresentConditionLayoutProcessor(getContext(), getFragmentManager(), contentBinding, record.getPerson());
        presentConditionLayoutProcessor.setOnSetBindingVariable(new OnSetBindingVariableListener() {
            @Override
            public void onSetBindingVariable(ViewDataBinding binding, String layoutName) {
                setRootNotificationBindingVariable(binding, layoutName);
                setLocalBindingVariable(binding, layoutName);
            }
        });

        contentBinding.setData(record);
        contentBinding.setPresentConditionClass(PresentCondition.class);
        contentBinding.setCheckedCallback(onPresentConditionCheckedCallback);
        contentBinding.setAddressLinkCallback(onAddressLinkClickedCallback);
    }

    @Override
    public void onAfterLayoutBinding(FragmentEventEditPersonInfoLayoutBinding binding) {
        binding.spnOccupationType.initialize(new TeboSpinner.ISpinnerInitConfig() {
            @Override
            public Object getSelectedValue() {
                return null;
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                return (occupationTypeList.size() > 0) ? DataUtils.toItems(occupationTypeList)
                        : DataUtils.toItems(occupationTypeList, false);
            }

            @Override
            public void onItemSelected(TeboSpinner view, Object value, int position, long id) {
                if (!occupationTypeLayoutProcessor.processLayout((OccupationType)value))
                    return;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spnGender.initialize(new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return null;
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                return (genderList.size() > 0) ? DataUtils.toItems(genderList)
                        : DataUtils.toItems(genderList, false);
            }
        });

        binding.spnAgeType.initialize(new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return null;
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                return (ageTypeList.size() > 0) ? DataUtils.toItems(ageTypeList)
                        : DataUtils.toItems(ageTypeList, false);
            }
        });

        binding.spnYear.initialize(new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return null;
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                return yearList;
            }
        });

        binding.spnMonth.initialize(new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return null;
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                return monthList;
            }
        });

        binding.spnDate.initialize(new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return null;
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                return dateList;
            }
        });

    }

    @Override
    public int getEditLayout() {
        return R.layout.fragment_event_edit_person_info_layout;
    }

    // <editor-fold defaultstate="collapsed" desc="Private Methods">

    private void setupCallback() {
        onPresentConditionCheckedCallback = new OnTeboSwitchCheckedChangeListener() {
            @Override
            public void onCheckedChanged(TeboSwitch teboSwitch, Object checkedItem, int checkedId) {
                if (checkedId < 0)
                    return;

                if (mLastCheckedId == checkedId) {
                    return;
                }

                mLastCheckedId = checkedId;

                if (!presentConditionLayoutProcessor.processLayout((PresentCondition)checkedItem))
                    return;
            }
        };

        onAddressLinkClickedCallback = new IEntryItemOnClickListener() {
            @Override
            public void onClick(View v, Object item) {
                final Location location = MemoryDatabaseHelper.LOCATION.getLocations(1).get(0);
                final LocationDialog locationDialog = new LocationDialog(TaskEditActivity.getActiveActivity(), location);
                locationDialog.show();


                locationDialog.setOnPositiveClickListener(new TeboAlertDialogInterface.PositiveOnClickListener() {
                    @Override
                    public void onOkClick(View v, Object item, View viewRoot) {
                        /*getContentBinding().txtAddress.setValue(location.toString());
                        locationDialog.dismiss();*/
                    }
                });
            }
        };
    }

    private void setLocalBindingVariable(final ViewDataBinding binding, String layoutName) {
        if (!binding.setVariable(BR.data, record.getPerson())) {
            Log.w(TAG, "There is no variable 'data' in layout " + layoutName);
        }
    }

    // </editor-fold>

    public static EventEditPersonsInvolvedInfoFragment newInstance(EventFormNavigationCapsule capsule)
            throws java.lang.InstantiationException, IllegalAccessException {
        return newInstance(EventEditPersonsInvolvedInfoFragment.class, capsule);
    }
}