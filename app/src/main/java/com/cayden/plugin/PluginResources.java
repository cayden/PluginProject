package com.cayden.plugin;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by cuiran
 * Time  17/3/14 17:37
 * Email cuiran2001@163.com
 * Description
 */

public class PluginResources extends Resources {

    public PluginResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
    }

    public static PluginResources getPluginResources(Resources resources,AssetManager assets){

        PluginResources pluginResources=new PluginResources(assets,resources.getDisplayMetrics(),resources.getConfiguration());

        return pluginResources;
    }

    public static AssetManager getPluginAssetManager(File apk) throws ClassNotFoundException{
      //需要通过反射获取AssetManager

       Class<?> forName=  Class.forName("android.content.res.AssetManager");

       Method[] methods= forName.getDeclaredMethods();
        for(Method method:methods){
            if(method.getName().equals("addAssetPath")){
                try{
                    AssetManager assetManager=AssetManager.class.newInstance();
                    method.invoke(assetManager,apk.getAbsolutePath());

                    return assetManager;
                }catch (InstantiationException e){
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        return null;

    }
}
