package de.symeda.sormas.app.component.dialog;

import java.util.List;

import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.component.Item;
import de.symeda.sormas.app.util.DataUtils;

/**
 * Created by Orson on 18/02/2018.
 *
 * www.technologyboard.org
 * sampson.orson@gmail.com
 * sampson.orson@technologyboard.org
 */

public class RegionLoader implements IRegionLoader {

    List<Item> regionList;
    private static RegionLoader sSoleInstance;

    private RegionLoader() {
        this.regionList = DataUtils.toItems(DatabaseHelper.getRegionDao().queryForAll(), false);
        //this.regionList = new ArrayList<>(MemoryDatabaseHelper.REGION.getRegions(5));
    }

    public static RegionLoader getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new RegionLoader();
        }

        return sSoleInstance;
    }

    @Override
    public List<Item> load() {
        return regionList;
    }
}