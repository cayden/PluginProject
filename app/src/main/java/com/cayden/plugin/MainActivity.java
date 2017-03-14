package com.cayden.plugin;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;


public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG=MainActivity.class.getSimpleName();
    private ImageView imageSource,imageCloud;
    private final static String SOURCE_TAG="source";
    private final static String CLOUD_TAG="cloud";
    private final static String CLOUD_ANIM="animation1";

    private final static String PLUGIN_NAME="plugin.apk";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initView();

    }

    private void initView(){
        imageSource=(ImageView)findViewById(R.id.imageSource);

        imageCloud=(ImageView)findViewById(R.id.imageCloud);

        imageSource.setTag(SOURCE_TAG);
        imageCloud.setTag(CLOUD_TAG);

        imageSource.setOnClickListener(this);
        imageCloud.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view.getTag().equals(SOURCE_TAG)){

            handleAnim(view);

        }else{

            //插件动画
            //是否加载
            String fileName=PLUGIN_NAME;
            String filePath=this.getCacheDir()+ File.separator+fileName;
            String packageName="com.cayden.pluginb";
            File apkFile=new File(filePath);
            if(apkFile.exists()){
               Drawable background= view.getBackground();
                if(background instanceof AnimationDrawable){
                    //执行动画
                    handleAnim(view);
                }else{
                    //执行插件
                    try{
                        AssetManager assetManager=PluginResources.getPluginAssetManager(apkFile);
                        PluginResources resources=  PluginResources.getPluginResources(getResources(),assetManager);

                        //反射文件
                        DexClassLoader classLoader=new DexClassLoader(apkFile.getAbsolutePath(),this.getDir(fileName, Context.MODE_PRIVATE).getAbsolutePath(),null,this.getClassLoader());

                        Class<?> loadClass= classLoader.loadClass(packageName+".R$drawable");
                        Field[] fields=  loadClass.getDeclaredFields();
                        for(Field field :fields){
                            if(field.getName().equals(CLOUD_ANIM)){
                                int animId=field.getInt(R.drawable.class);
                                Drawable drawable=resources.getDrawable(animId);
                                ((ImageView) view).setBackgroundDrawable(drawable);
                                handleAnim(view);
                            }
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else{
                //需要从服务端下载，测试就放入assets目录下
                try{
                    InputStream is=this.getAssets().open(fileName);
                    FileOutputStream os=new FileOutputStream(filePath);
                    int len=0;
                    byte [] buffer=new byte[1024];
                    while((len=is.read(buffer))!=-1){
                        os.write(buffer,0,len);
                    }
                    os.close();
                    is.close();
                    Log.d(TAG,"file ok");
                    Toast.makeText(this,"file ok",Toast.LENGTH_SHORT).show();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }



        }
    }

    private void handleAnim(View v){
        AnimationDrawable background=(AnimationDrawable)v.getBackground();
        if(background!=null){
            if(background.isRunning() ){
                background.stop();
            }else{
                background.stop();
                background.start();
            }
        }
    }

}
