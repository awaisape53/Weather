package top.maweihao.weather.entity;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import top.maweihao.weather.entity.NewWeatherRealtime;
import top.maweihao.weather.entity.HeWeather.NewHeWeatherNow;
import top.maweihao.weather.entity.NewWeather;

import top.maweihao.weather.entity.NewWeatherRealtimeDao;
import top.maweihao.weather.entity.HeWeather.NewHeWeatherNowDao;
import top.maweihao.weather.entity.NewWeatherDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig newWeatherRealtimeDaoConfig;
    private final DaoConfig newHeWeatherNowDaoConfig;
    private final DaoConfig newWeatherDaoConfig;

    private final NewWeatherRealtimeDao newWeatherRealtimeDao;
    private final NewHeWeatherNowDao newHeWeatherNowDao;
    private final NewWeatherDao newWeatherDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        newWeatherRealtimeDaoConfig = daoConfigMap.get(NewWeatherRealtimeDao.class).clone();
        newWeatherRealtimeDaoConfig.initIdentityScope(type);

        newHeWeatherNowDaoConfig = daoConfigMap.get(NewHeWeatherNowDao.class).clone();
        newHeWeatherNowDaoConfig.initIdentityScope(type);

        newWeatherDaoConfig = daoConfigMap.get(NewWeatherDao.class).clone();
        newWeatherDaoConfig.initIdentityScope(type);

        newWeatherRealtimeDao = new NewWeatherRealtimeDao(newWeatherRealtimeDaoConfig, this);
        newHeWeatherNowDao = new NewHeWeatherNowDao(newHeWeatherNowDaoConfig, this);
        newWeatherDao = new NewWeatherDao(newWeatherDaoConfig, this);

        registerDao(NewWeatherRealtime.class, newWeatherRealtimeDao);
        registerDao(NewHeWeatherNow.class, newHeWeatherNowDao);
        registerDao(NewWeather.class, newWeatherDao);
    }
    
    public void clear() {
        newWeatherRealtimeDaoConfig.clearIdentityScope();
        newHeWeatherNowDaoConfig.clearIdentityScope();
        newWeatherDaoConfig.clearIdentityScope();
    }

    public NewWeatherRealtimeDao getNewWeatherRealtimeDao() {
        return newWeatherRealtimeDao;
    }

    public NewHeWeatherNowDao getNewHeWeatherNowDao() {
        return newHeWeatherNowDao;
    }

    public NewWeatherDao getNewWeatherDao() {
        return newWeatherDao;
    }

}