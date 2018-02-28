package de.symeda.sormas.app.caze.edit;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import de.symeda.sormas.app.BR;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.component.Item;
import de.symeda.sormas.app.component.TeboButtonType;
import de.symeda.sormas.app.component.TeboSpinner;
import de.symeda.sormas.app.component.dialog.BaseTeboAlertDialog;
import de.symeda.sormas.app.component.dialog.CommunityLoader;
import de.symeda.sormas.app.component.dialog.DistrictLoader;
import de.symeda.sormas.app.component.dialog.FacilityLoader;
import de.symeda.sormas.app.component.dialog.ICommunityLoader;
import de.symeda.sormas.app.component.dialog.IDistrictLoader;
import de.symeda.sormas.app.component.dialog.IFacilityLoader;
import de.symeda.sormas.app.component.dialog.IRegionLoader;
import de.symeda.sormas.app.component.dialog.RegionLoader;
import de.symeda.sormas.app.databinding.DialogPreviousHospitalizationLayoutBinding;
import de.symeda.sormas.app.util.DataUtils;

import java.util.List;

import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.app.backend.facility.Facility;
import de.symeda.sormas.app.backend.hospitalization.PreviousHospitalization;
import de.symeda.sormas.app.backend.region.Community;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.backend.region.Region;

/**
 * Created by Orson on 18/02/2018.
 * <p>
 * www.technologyboard.org
 * sampson.orson@gmail.com
 * sampson.orson@technologyboard.org
 */

public class PreviousHospitalizationDialog extends BaseTeboAlertDialog {

    public static final String TAG = PreviousHospitalizationDialog.class.getSimpleName();

    private PreviousHospitalization data;


    private IRegionLoader regionLoader;
    private IDistrictLoader districtLoader;
    private ICommunityLoader communityLoader;
    private IFacilityLoader facilityLoader;

    public PreviousHospitalizationDialog(final FragmentActivity activity, PreviousHospitalization previousHospitalization) {
        this(activity, R.string.heading_case_hos_prev_hospitalization, -1, null,
                null, null, null, previousHospitalization);
    }


    public PreviousHospitalizationDialog(final FragmentActivity activity, IRegionLoader regionLoader,
                          IDistrictLoader districtLoader, ICommunityLoader communityLoader, IFacilityLoader facilityLoader,
                                         PreviousHospitalization previousHospitalization) {
        this(activity, R.string.heading_case_hos_prev_hospitalization, -1, regionLoader, districtLoader,
                communityLoader, facilityLoader, previousHospitalization);
    }

    public PreviousHospitalizationDialog(final FragmentActivity activity, int headingResId, int subHeadingResId,
                          IRegionLoader regionLoader, IDistrictLoader districtLoader,
                          ICommunityLoader communityLoader, IFacilityLoader facilityLoader, PreviousHospitalization previousHospitalization) {
        super(activity, R.layout.dialog_root_layout, R.layout.dialog_previous_hospitalization_layout,
                R.layout.dialog_root_three_button_panel_layout, headingResId, subHeadingResId);

        this.regionLoader = regionLoader == null? RegionLoader.getInstance() : regionLoader;
        this.districtLoader = districtLoader == null? DistrictLoader.getInstance() : districtLoader;
        this.communityLoader = communityLoader == null? CommunityLoader.getInstance() : communityLoader;
        this.facilityLoader = facilityLoader == null? FacilityLoader.getInstance() : facilityLoader;

        this.data = previousHospitalization;
    }

    @Override
    protected void onOkClicked(View v, Object item, View rootView, ViewDataBinding contentBinding) {
        /*DialogPreviousHospitalizationLayoutBinding _contentBinding = (DialogPreviousHospitalizationLayoutBinding)contentBinding;

        _contentBinding.spnState.enableErrorState("Hello");*/
        dismiss();
    }

    @Override
    protected void onDismissClicked(View v, Object item, View rootView, ViewDataBinding contentBinding) {

    }

    @Override
    protected void onDeleteClicked(View v, Object item, View rootView, ViewDataBinding contentBinding) {

    }

    @Override
    protected void setBindingVariable(Context context, ViewDataBinding binding, String layoutName) {
        if (!binding.setVariable(BR.data, data)) {
            Log.w(TAG, "There is no variable 'data' in layout " + layoutName);
        }

        if (!binding.setVariable(BR.yesNoUnknownClass, YesNoUnknown.class)) {
            Log.w(TAG, "There is no variable 'data' in layout " + layoutName);
        }
    }

    @Override
    protected void initializeContentView(ViewDataBinding rootBinding, ViewDataBinding contentBinding, ViewDataBinding buttonPanelBinding) {
        DialogPreviousHospitalizationLayoutBinding _contentBinding = (DialogPreviousHospitalizationLayoutBinding)contentBinding;

        _contentBinding.dtpDateOfAdmission.initialize(getFragmentManager());
        _contentBinding.dtpDateOfDischarge.initialize(getFragmentManager());


        _contentBinding.spnState.initialize(new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return data.getRegion();
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                /*Community comm = DatabaseHelper.getCommunityDao().queryForAll().get(0);
                return DataUtils.toItems(DatabaseHelper.getFacilityDao()
                        .getHealthFacilitiesByCommunity(comm, false));*/


                //return DataUtils.toItems(communityList);

                List<Region> regions = regionLoader.load();

                //TODO: Remove, Just for testing
                if (regions.size() > 0 && regions.get(0) != null)
                    data.setRegion(regions.get(0));


                return (regions.size() > 0) ? DataUtils.toItems(regions) : DataUtils.toItems(regions, false);
            }
        });

        _contentBinding.spnLga.initialize(_contentBinding.spnState, new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return data.getDistrict();
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                List<District> districts = districtLoader.load((Region)parentValue);

                //TODO: Remove, Just for testing
                if (districts.size() > 0 && districts.get(0) != null)
                    data.setDistrict(districts.get(0));

                return (districts.size() > 0) ? DataUtils.toItems(districts) : DataUtils.toItems(districts, false);

            }
        });

        _contentBinding.spnWard.initialize(_contentBinding.spnLga, new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return data.getCommunity();
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                List<Community> communities = communityLoader.load((District)parentValue);

                //TODO: Remove, Just for testing
                if (communities.size() > 0 && communities.get(0) != null)
                    data.setCommunity(communities.get(0));

                return (communities.size() > 0) ? DataUtils.toItems(communities) : DataUtils.toItems(communities, false);
            }
        });

        _contentBinding.spnFacility.initialize(_contentBinding.spnWard, new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return null;
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                List<Facility> facilities = facilityLoader.load((Community)parentValue);

                //TODO: Remove, Just for testing
                if (facilities.size() > 0 && facilities.get(0) != null)
                    data.setHealthFacility(facilities.get(0));

                return (facilities.size() > 0) ? DataUtils.toItems(facilities) : DataUtils.toItems(facilities, false);
            }
        });
    }

    @Override
    public boolean isOkButtonVisible() {
        return true;
    }

    @Override
    public boolean isDismissButtonVisible() {
        return true;
    }

    @Override
    public boolean isDeleteButtonVisible() {
        return true;
    }

    @Override
    public boolean isRounded() {
        return true;
    }

    @Override
    public TeboButtonType dismissButtonType() {
        return TeboButtonType.BTN_LINE_SECONDARY;
    }

    @Override
    public TeboButtonType okButtonType() {
        return TeboButtonType.BTN_LINE_PRIMARY;
    }

    @Override
    public TeboButtonType deleteButtonType() {
        return TeboButtonType.BTN_LINE_DANGER;
    }

    /*public PreviousHospitalizationDialog(final FragmentActivity activity, List<Region> regionList,
                                         List<District> districtList, List<Community> communityList,
                                         List<Facility> facilityList, Case caze) {
        this(activity, R.string.heading_case_hos_prev_hospitalization, -1, regionList, districtList,
                communityList, facilityList, caze);
    }

    public PreviousHospitalizationDialog(final FragmentActivity activity, int headingResId, int subHeadingResId,
                                         List<Region> regionList, List<District> districtList, List<Community> communityList,
                                         List<Facility> facilityList, Case caze) {
        super(activity, R.layout.dialog_root_layout, R.layout.dialog_previous_hospitalization_layout,
                R.layout.dialog_root_three_button_panel_layout, headingResId, subHeadingResId);

        this.data = caze;
        this.regionList = regionList;
        this.districtList = districtList;
        this.communityList = communityList;
        this.facilityList = facilityList;
    }*/

}