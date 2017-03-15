#PluginProject
###开篇介绍
现在项目比较大 资源比较多，但是若希望动态来加载资源文件，可以有以下几种方式:
1. 通过下载资源文件zip包然后解压来加载
2. 通过插件开发
本文通过插件开发来实现加载插件中的资源文件.
###程序演示
可以打开链接 [效果演示](http://weibo.com/tv/v/EzNwkq0oP?fid=1034:8c610fcd0d501a61a67ddf56da6e4225"optional title")
打开后显示2个动画，上面的动画是加载的本地动画，下面的动画是从插件里面加载的。
###代码介绍
如图所示：
![程序结构](http://img.blog.csdn.net/20170314222630155?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3VpcmFu/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
工程app作为宿主程序，plugin作为插件程序，资源文件也在plugin里面，需要实现的是启动app来加载插件plugin里面的资源文件

这里实现思路大致画一下，需要在主程序里面加载插件程序的资源文件，我们需要拿到插件程序里面的Context, 获取资源文件也就是AssetManager，这里需要用到java的反射机制
![思路](http://img.blog.csdn.net/20170314223011484?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvY3VpcmFu/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

这里给出核心代码部分
PluginResources 类如下
```
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

```

启动MainActivity类如下

```
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

```
为了演示方便 我们把plugin.apk放在主程序app工程的assets目录下。


最后给出项目的源码地址：[https://github.com/cayden/PluginProject](https://github.com/cayden/PluginProject)
