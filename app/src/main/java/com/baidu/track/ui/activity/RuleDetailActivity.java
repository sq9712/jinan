package com.baidu.track.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.track.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RuleDetailActivity extends Activity implements View.OnClickListener {
    private LinearLayout btn_back;
    private TextView txt_title;
    private Button btn_top;
    private TextView wView;
    private String title,standardcontent,category;
    private String text ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        bindViews();
    }

    private void bindViews() {
        Bundle bundle=getIntent().getExtras();
        category = bundle.getString("category");
        title=bundle.getString("legal");

        btn_back = findViewById(R.id.btn_back);
        txt_title = findViewById(R.id.txt_title);
        btn_top = findViewById(R.id.btn_top);
        txt_title.setText(title);
        wView = findViewById(R.id.wView);
        wView.setMovementMethod(ScrollingMovementMethod.getInstance());

        btn_back.setOnClickListener(this);
        btn_top.setOnClickListener(this);
        if(category.equals("治理标准")){

            standardcontent = bundle.getString("standardcontent");
            String[] str = standardcontent.split("[|]");
            for (int i=0;i<str.length;i++){
                text = text+str[i]+"\n"+"\n";
            }
            wView.setText(text);
        }else {
            if (title.equals("《无证无照经营查处办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule1))
                );
            } else if (title.equals("《食品安全法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule2))
                );
            } else if (title.equals("《城市道路管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule3))
                );
            } else if (title.equals("《环境噪声污染防治法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule4))
                );
            } else if (title.equals("《山东省环境噪声污染防治条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule5))
                );
            } else if (title.equals("《卫星电视广播地面接收设施管理规定》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule6))
                );
            } else if (title.equals("《城市建筑垃圾和工程渣土管理规定》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule7))
                );
            }else if (title.equals("《土地管理法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule8))
                );
            }else if (title.equals("《城乡规划法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule9))
                );
            }else if (title.equals("《土地管理法实施条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule10))
                );
            }else if (title.equals("《基本农田保护条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule11))
                );
            }else if (title.equals("《国土资源部最高人民检察院公安部关于国土资源行政主管部门移送涉嫌国土资源犯罪案件的若干意见》(国土资发(2008)203号)")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule12))
                );
            }else if (title.equals("《森林病虫害防治条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule13))
                );
            } else if (title.equals("《突发林业有害生物事件处置办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule14))
                );
            } else if (title.equals("《森林法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule15))
                );
            }else if (title.equals("《森林采伐更新管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule16))
                );
            } else if (title.equals("《矿产资源法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule17))
                );
            }else if (title.equals("《最高人民法院最高人民检察院关于办理非法采矿、破坏性采矿刑事案件适用法律若干问题的解释》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule18))
                );
            }else if (title.equals("《河道管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule19))
                );
            } else if (title.equals("《山东省水资源条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule20))
                );
            }else if (title.equals("《水利部关于河道采砂管理工作指导意见》(水河湖(2019)58号)")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule21))
                );
            }else if (title.equals("《环境保护法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule22))
                );
            }else if (title.equals("《水污染防治法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule23))
                );
            }else if (title.equals("《饮用水源保护区污染防治管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule24))
                );
            }else if (title.equals("《固体废物污染环境防治法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule25))
                );
            }else if (title.equals("《省政府办公厅关于印发山东省危险废物专项排查整治方案的通知》（鲁政办字（2019）58号）")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule26))
                );
            }else if (title.equals("《大气污染防治法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule27))
                );
            }else if (title.equals("《山东省大气污染防治条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule28))
                );
            }else if (title.equals("《山东省机动车排气污染防治条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule29))
                );
            }else if (title.equals("《环境影响评价法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule30))
                );
            }else if (title.equals("《山东省环境保护条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule31))
                );
            }else if (title.equals("《畜禽规模养殖污染防治条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule32))
                );
            }else if (title.equals("《国务院办公厅关于加快推进畜禽养殖废弃物资源化利用的意见》(国办发(2017)48号)")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule33))
                );
            }else if (title.equals("《山东省乡村建设工程质量安全管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule34))
                );
            }else if (title.equals("《山东省农村危房改造补助资金管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule35))
                );
            }else if (title.equals("《住建部财政部关于印发农村危房改造脱贫攻坚三年行动方案的通知》(建村(2018)115号)")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule36))
                );
            }else if (title.equals("《物业管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule37))
                );
            }else if (title.equals("《住宅室内装饰装修管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule38))
                );
            }else if (title.equals("《山东省城乡规划条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule39))
                );
            }else if (title.equals("《安全生产法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule40))
                );
            }else if (title.equals("《山东省安全生产条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule41))
                );
            }else if (title.equals("《烟花爆竹安全管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule42))
                );
            }else if (title.equals("《山东省燃气管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule43))
                );
            }else if (title.equals("《危险化学品安全管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule44))
                );
            }else if (title.equals("《山东省危险化学品安全管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule45))
                );
            }else if (title.equals("《道路交通安全法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule46))
                );
            }else if (title.equals("《消防法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule47))
                );
            }else if (title.equals("《山东省燃气管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule48))
                );
            }else if (title.equals("《安全生产事故救援条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule49))
                );
            }else if (title.equals("《地方党政领导干部食品安全责任制规定》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule50))
                );
            }else if (title.equals("《山东省食品小作坊、小餐饮和食品摊点管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule51))
                );
            }else if (title.equals("《食品生产经营日常监督检查管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule52))
                );
            }else if (title.equals("《山东省餐饮服务食品安全监督量化分级和登记公示管理规定》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule53))
                );
            }else if (title.equals("《网络餐饮服务食品安全监督管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule54))
                );
            }else if (title.equals("《食品安全抽样检验管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule55))
                );
            }else if (title.equals("《山东省学校食堂食品安全监督检查办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule56))
                );
            }else if (title.equals("《药品管理法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule57))
                );
            }else if (title.equals("《药品管理法实施条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule58))
                );
            }else if (title.equals("《化妆品卫生监督条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule59))
                );
            }else if (title.equals("《山东省药品使用条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule60))
                );
            }else if (title.equals("《医疗器械监督管理条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule61))
                );
            }else if (title.equals("《特种设备安全法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule62))
                );
            }else if (title.equals("《特种设备安全监察条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule63))
                );
            }else if (title.equals("《山东省特种设备安全条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule64))
                );
            }else if (title.equals("《消费者权益保护法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule65))
                );
            }else if (title.equals("《侵害消费者权益行为处罚办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule66))
                );
            }else if (title.equals("《山东省消费者权益保护条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule67))
                );
            }else if (title.equals("《广告法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule68))
                );
            }else if (title.equals("《广告发布登记管理规定》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule69))
                );
            }else if (title.equals("《价格法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule70))
                );
            }else if (title.equals("《价格违法行为行政处罚规定》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule71))
                );
            }else if (title.equals("《禁止价格欺诈行为的规定》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule72))
                );
            }else if (title.equals("《水法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule73))
                );
            }else if (title.equals("《防洪法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule74))
                );
            }else if (title.equals("《森林防火条例》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule75))
                );
            }else if (title.equals("《山东省实施森林防火条例办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule76))
                );
            }else if (title.equals("《治安处罚法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule77))
                );
            }else if (title.equals("《刑法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule78))
                );
            }else if (title.equals("《节约能源法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule79))
                );
            }else if (title.equals("《饮用水水源保护区污染防治管理规定》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule80))
                );
            }else if (title.equals("《成品油市场管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule81))
                );
            }else if (title.equals("《山东省成品油市场管理办法》")) {
                wView.setText(
                        readStream(getResources().openRawResource(R.raw.rule82))
                );
            }
        }
    }


    @Override
    public void onClick (View v){
        switch (v.getId()) {
            case R.id.btn_back:
                finish();          //关闭当前Activity
                break;
            case R.id.btn_top:
                wView.setScrollY(0);   //滚动到顶部
                break;
        }
    }
    //读写流
    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }
}
