package com.planegg.puzzle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;
    int pildid[]={R.drawable.p1,R.drawable.p2,R.drawable.p3,R.drawable.p4,R.drawable.p5,R.drawable.p6,
            R.drawable.p7,R.drawable.p8,R.drawable.p9};
    int iMazda=R.drawable.mazda;
    Button b,bAlusta;
    ImageView img;
    Bitmap bMap;
    Timer timer;
    int ipilteHorisontaalis=3,ipilteVertikaalis=3;
    int iSuurPiltLaius=400, iSuurPiltKorgus=300;
    int ivaikePiltLaius=100,ivaikePiltKorgus=100;
    int iPiltideVahe=0; // pildikeste vahe üksteisest
    RelativeLayout rl;
    LinearLayout ll;
    TextView txtKell,txtInfo;
    private int xDelta, yDelta;
    ArrayList <Asukoht> tykid = new ArrayList<>();
    Asukoht tykk_tyhi;
    private String sKood3;
    private boolean bMangKaib;
    private int iSekund;
    private int iSegamisLiikumisi=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rl=(RelativeLayout)findViewById(R.id.rl_plats);
        ll=(LinearLayout)findViewById(R.id.ll_peamine) ;
        b=(Button)findViewById(R.id.button);
        bAlusta=findViewById(R.id.bAlusta);
        txtKell=findViewById(R.id.txtKell);
        txtInfo=findViewById(R.id.txtDebug);
        bMangKaib=false;
        doTekitaPildid();
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGetPiltfromGallery();
            }
        });
        bAlusta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAlustaMangu();
            }

        });
    }

    private void doTyhiKohtInit(int iProovKoht)
    {   int iTyhi;
        Random r = new Random();
        iTyhi=r.nextInt(rl.getChildCount());
        iTyhi=iProovKoht;
        View v=rl.getChildAt(iTyhi);
        tykk_tyhi=new Asukoht(v.getId(),v.getLeft(),v.getTop());
    }
    private void doEemaldaTyhiElement(boolean bGONE)
    { int iTyhi;
        iTyhi=getViewIDbygetID(tykk_tyhi.getId());
        if (iTyhi!=-1)
        {
            View v=rl.getChildAt(iTyhi);
            if (bGONE)
            {
                v.setVisibility(View.GONE);
            }else
            {
                v.setVisibility(View.VISIBLE);
            }

        }else
        {
            // mingi viga, siia ei tohiks jõuda
            System.out.println("N99 viga");
        }

    }
    private void doSegaPildid(int iKaikUus)
    {   int iTyhiViewID,iNaaberTykk,iKaik;
        View v,v1,v2;
        Random r = new Random();
        // suvaline pilt peidetakse ära


        //tyhi 7 kaik 1, tyhi 7 kaik 4
      //  for (int i=0;i<iSegamisLiikumisi;i++)
        {
            //test 1 tyhi 7 kaik 1,
            iTyhiViewID=getViewIDbygetID( tykk_tyhi.getId());
            iKaik=iKaikUus;//r.nextInt(4)+1;
            iNaaberTykk=getiLeiaNaaberTykk(iKaik,iTyhiViewID);

            if (iTyhiViewID >-1 && iNaaberTykk >-1 )
            {
                doSwapTykid(iTyhiViewID,iNaaberTykk);
            }

        }



    }
    private void doAlustaMangu() {
        //doSegaPildid();
        bMangKaib=true;
        iSekund=0;
    }
    private void doTekitaPildid() {
        int iLoendur=0;
        int iHMax=0;

        for (int i=0;i<3;i++)
        {
            for (int j=0;j<3;j++)
            {
                ImageView im=new ImageView(getApplicationContext());
                im.setImageDrawable(getDrawable(pildid[iLoendur]));

               // im.setPadding(i*im.getDrawable().getMinimumWidth(),j*im.getDrawable().getMinimumHeight(),0,0);
                RelativeLayout.LayoutParams params=
                        new RelativeLayout.LayoutParams(im.getDrawable().getMinimumWidth(),
                                                        im.getDrawable().getMinimumHeight());
                params.leftMargin=j*(im.getDrawable().getMinimumWidth()+iPiltideVahe);
                params.topMargin=i*(im.getDrawable().getMinimumHeight()+iPiltideVahe);
               im.setLayoutParams(params);
                im.setId(iLoendur+1);
                tykid.add(new Asukoht(im.getId(),params.leftMargin,params.topMargin));
                im.setOnTouchListener(new onTouchListener());

                rl.addView(im);

                System.out.println("N98 i:"+i+" j:"+j+" iLoendur:"+iLoendur);

                iLoendur++;
            }

        }
        rl.invalidate();
        doKorrigeeriRl();
        System.out.println("N98 HMax:"+iHMax);
    }


    public void doKorrigeeriRl()
    {
        for (int i=0;i<rl.getChildCount();i++)
        {
            View v=rl.getChildAt(i);
            if (rl.getMinimumHeight()<v.getTop()+v.getHeight()+iPiltideVahe)
            {
                rl.setMinimumHeight(v.getTop()+v.getHeight()+iPiltideVahe*2);
            }
        }
    }

    private void doGetPiltfromGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
        rl.setBackground(getDrawable(pildid[5]));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            Bundle extras = data.getExtras();


            try {
                InputStream inputStream=getContentResolver().openInputStream(data.getData());
                Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                if (imageBitmap!=null)
                {
                    doTaidaPildid(imageBitmap);
                }
                imageBitmap=Bitmap.createScaledBitmap(imageBitmap,300,300,true);


                for (int i=0;i<rl.getChildCount();i++)
                {
                    bMap=Bitmap.createBitmap(imageBitmap,i*10,i*10,100,100);
                    ImageView v=(ImageView)rl.getChildAt(i);
                    v.setImageBitmap(bMap);
                }
                System.out.println("N98 oleme pildi laadind");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private void doTaidaPildid(Bitmap imageBitmap) {
        int iLoendur=0;
        int iHMax=0;
        rl.removeAllViews();

        for (int i=0;i<3;i++)
        {
            for (int j=0;j<3;j++)
            {
                ImageView im=new ImageView(getApplicationContext());
                Bitmap bm=Bitmap.createBitmap(imageBitmap,j*100,i*100,100,100);


                im.setImageBitmap(bm);

                // im.setPadding(i*im.getDrawable().getMinimumWidth(),j*im.getDrawable().getMinimumHeight(),0,0);
                RelativeLayout.LayoutParams params=
                        new RelativeLayout.LayoutParams(im.getDrawable().getMinimumWidth(),
                                im.getDrawable().getMinimumHeight());
                params.leftMargin=j*(im.getDrawable().getMinimumWidth()+iPiltideVahe);
                params.topMargin=i*(im.getDrawable().getMinimumHeight()+iPiltideVahe);

                im.setLayoutParams(params);
                im.setId(iLoendur+1);
                tykid.add(new Asukoht(im.getId(),params.leftMargin,params.topMargin));
                im.setOnTouchListener(new onTouchListener());
                if ( iLoendur+1==6 )
                {
                    // im.setVisibility(View.GONE);
                }
                rl.addView(im);

                System.out.println("N98 i:"+i+" j:"+j+" iLoendur:"+iLoendur);

                iLoendur++;
            }

        }
        rl.invalidate();
    }

    public boolean isCollision(int id,int xLeft,int yTop,int xWidth,int yHeight) {
      boolean bRet=false;
        for (int i=0;i<rl.getChildCount();i++)
        {
            View v=rl.getChildAt(i);
            if (v.getId()!=id && v.getVisibility()==View.VISIBLE) // iseendaga ei ole vaja kontrollida
            {
                Rect R1=new Rect(xLeft, yTop, xLeft+xWidth, yTop+yHeight);
                Rect R2=new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                if (R1.intersect(R2))
                {

                    bRet=true;
                    break;
                }
            }

        }
        return bRet;
    }
    private class onTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean xCollision, yCollision;
            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_DOWN: {
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

                    xDelta = x - lParams.leftMargin;
                    yDelta = y - lParams.topMargin;
                    System.out.println("N98 vajutati: id:"+v.getId());
                    break;

                }
                case MotionEvent.ACTION_UP: {
                    // jupp lasti lahti
                    doKorrigeeriAsukohad(v.getId());

                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (x - xDelta + v.getWidth() <= rl.getWidth()
                            &&
                            y - yDelta + v.getHeight() <= rl.getHeight()
                            && x - xDelta >= 0
                            && y - yDelta >= 0
                      || 1==1) {
                        RelativeLayout.LayoutParams layoutParams =
                                (RelativeLayout.LayoutParams) v.getLayoutParams();
                        // Rect intersectiga teha, x puhul anda parameetrix x-xDelta,params.topmargin
                      //  if (!isXCollision(v.getId(),x - xDelta,y - yDelta, layoutParams.width,layoutParams.height))
                        // if (!isCollision(v.getId(),x - xDelta,y - yDelta, layoutParams.width,layoutParams.height))
                        if (!isCollision(v.getId(),x - xDelta,v.getTop(), layoutParams.width,layoutParams.height)
                        && x - xDelta + v.getWidth() <= rl.getWidth()
                        &&  x - xDelta >= 0)
                        {
                            layoutParams.leftMargin = x - xDelta;
                        }
                        // Rect intersectiga teha, x puhul anda parameetrix params.leftmargin,y-yDelta
                       // if (!isYCollision(v.getId(),x - xDelta,y - yDelta, layoutParams.width,layoutParams.height))
                        if (!isCollision(v.getId(),v.getLeft(),y - yDelta, layoutParams.width,layoutParams.height)
                            &&   y - yDelta >= 0 &&
                                y - yDelta + v.getHeight() <= rl.getHeight())
                        {
                            layoutParams.topMargin = y - yDelta;
                        }

                        layoutParams.rightMargin = 0;
                        layoutParams.bottomMargin = 0;
                        v.setLayoutParams(layoutParams);

                    }
                    break;
                }

            }
            rl.invalidate();

                return true;


        }
    }

    private boolean KontrolliKasPiltOige() {
        boolean bRet=true;
        for (int i=0;i<rl.getChildCount();i++)
        {
            View v=rl.getChildAt(i);
            for (int j=0;j<tykid.size();j++)
            {
                if (v.getId()==tykid.get(j).getId())
                {
                    if (v.getLeft()!=tykid.get(i).getX() ||
                            v.getTop()!=tykid.get(i).getY())
                    {
                        bRet=false;
                        break;
                    }
                }
            }

        }


        if (bRet)
        {
            doLopetaMang();

        }
        /*if (!bRet)
        {
            for (int i=0;i<rl.getChildCount();i++)
            {
                View v=rl.getChildAt(i);
                System.out.println("N99 v.id:"+v.getId()+" x:"+v.getLeft()+" y:"+v.getTop());
            }
            for (int i=0;i<tykid.size();i++)
            {
                System.out.println("N99 tykk id:"+tykid.get(i).getId()+" x:"+tykid.get(i).getX()
                        +" y:"+tykid.get(i).getY());
            }
        }*/
        return bRet;
    }

    private void doKorrigeeriAsukohad(int id) {
        // kui jupp on peaaegu omal kohal, siis paneme ta täpselt sinna
        int iKorrekt=0;
        for (int i=0;i<rl.getChildCount();i++)
        {

            View v=rl.getChildAt(i);
            if (v.getId()==id)
            {
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                iKorrekt=(v.getLeft()+(int)((v.getWidth()*0.2f)))/v.getWidth();
                iKorrekt*=v.getWidth();

                lParams.leftMargin=iKorrekt;

                iKorrekt=(v.getTop()+(int)((v.getHeight()*0.2f)))/v.getHeight();
                iKorrekt*=v.getHeight();

                lParams.topMargin=iKorrekt;
                v.setLayoutParams(lParams);
                v.invalidate();
                rl.invalidate();
            }

        }
    }


    private boolean isXCollision(int _id,int xMarg, int yMarg, int xWidth, int yHeight) {
        boolean bRet=false;
        int xViga=6,yViga=6;
        int childCount = rl.getChildCount();

        // käib kõik pildid läbi, et leida, kas selles kohas xMarginaal ei kattu
        for (int i=0;i<childCount;i++)
        {
            View v = rl.getChildAt(i);
            if (v.getId()!=_id && v.getVisibility()==View.VISIBLE)
            {
                // Y kattuvuse kontroll

                if (v.getTop()<yMarg+ yHeight-yViga // otsitava ülemine serv minu alumisest väiksem
                        && v.getTop()+v.getHeight()>yMarg-yViga ) // otsitava alumine serv minu ülemisest suurem
                {
                    // y on samas alas
                    if ( v.getLeft()+v.getWidth()>xMarg+xViga      // otsitava parem serv minu vasakust suurem
                            && v.getLeft() <xMarg+xWidth-xViga) // otsitava vasak serv minu paremast väiksem
                    {
                        // see asub minust paremal kõrval
                        bRet=true;
                        break;
                    }
                }
            }
        }

        return bRet;
    }

    private boolean isYCollision(int _id,int xMarg, int yMarg, int xWidth, int yHeight) {
        boolean bRet=false;
        int xViga=6,yViga=6;
        int childCount = rl.getChildCount();

        // käib kõik pildid läbi, et leida, kas selles kohas yMarginaal ei kattu
        for (int i=0;i<childCount;i++)
        {
            View v = rl.getChildAt(i);
            if (v.getId()!=_id && v.getVisibility()==View.VISIBLE)
            {
                // X kattuvuse kontroll
                if ( v.getLeft()+v.getWidth()>xMarg+xViga      // otsitava parem serv minu vasakust suurem
                        && v.getLeft() <xMarg+xWidth-xViga) // otsitava vasak serv minu paremast väiksem
                {
                    // y on samas alas
                    if (v.getTop()<yMarg+ yHeight-yViga // otsitava ülemine serv minu alumisest väiksem
                            && v.getTop()+v.getHeight()>yMarg-yViga ) // otsitava alumine serv minu ülemisest suurem
                    {
                        // see asub minust paremal kõrval
                        bRet=true;
                        break;
                    }
                }
            }
        }

        return bRet;
    }

    @Override
    protected void onStart() {
        super.onStart();
        doKaivitaTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer!=null)
        {
            timer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer!=null)
        {
            timer.cancel();
        }
    }

    private void doKaivitaTimer() {
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                doTaimeriTegevus();
            }
        },1000,1000); // kord sekundis
    }

    private void doTaimeriTegevus() {
        if (bMangKaib)
        {
            iSekund++; // kell tiksub
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doInfo();
                    if (iSekund==2)
                    {
                        doTyhiKohtInit(0);
                    }

                    if (iSekund==3)
                    {
                        doEemaldaTyhiElement(true);
                    }

                    if (iSekund==5) {
                       // doPaneTyhiTykkTagasi();
                        doEemaldaTyhiElement(false);
                    }
                    if (iSekund==8)
                    {
                        doSegaPildid(2);
                        //  doEemaldaYksPilt(7);
                    }

                    if (iSekund==12)
                    {
                        doEemaldaTyhiElement(true);
                    }

                    if (iSekund==15) {
                        doEemaldaTyhiElement(false);
                      //  doPaneTyhiTykkTagasi();
                    }
                    if (iSekund>25)
                    {
                        if (KontrolliKasPiltOige())
                        {
                            doLopetaMang();
                        }
                    }


                    // näitame sekundeid
                    if (txtKell!=null)
                    {
                        txtKell.setText(iSekund+" sekundit");
                    }
                }
            });

        }
    }

    private void doInfo() {
        if (tykk_tyhi!=null)
        {
            txtInfo.setText(" Tyhi id:"+tykk_tyhi.getId()+" X:"+tykk_tyhi.getX() + "Y:"+tykk_tyhi.getY());
        }
    }

    private void doSwapTykid(int idTyhi,int id2)
    {   boolean b=false;
        // vahetab tükkide koordinaadid omavahel ära
        // tükk EI pea olema VISIBLE
        View vTyhi=rl.getChildAt(idTyhi);
        View v2=rl.getChildAt(id2);

        if (vTyhi!=null && v2!=null) {
            RelativeLayout.LayoutParams lParams1 =
                    (RelativeLayout.LayoutParams) vTyhi.getLayoutParams();
            RelativeLayout.LayoutParams lParams2 =
                    (RelativeLayout.LayoutParams) v2.getLayoutParams();
            lParams1.topMargin = v2.getTop();
            lParams1.leftMargin = v2.getLeft();

            lParams2.topMargin = vTyhi.getTop();
            lParams2.leftMargin = vTyhi.getLeft();

            tykk_tyhi.setXY(lParams1.leftMargin,lParams1.topMargin); // jätame tühja koha meelde


            vTyhi.setLayoutParams(lParams1);
            vTyhi.invalidate();



            v2.setLayoutParams(lParams2);
            v2.invalidate();

        }

    }
    private int getiLeiaTyhiID()
    {// leiab tühja view
        int iRet;
        iRet=-1;
         for (int i=0;i<rl.getChildCount();i++)
         {
             View v=rl.getChildAt(i);
             if (v.getVisibility()==View.GONE)
             {
                 iRet=i;
                 break;
             }
         }
        return iRet;
    }
    private int getiLeiaNaaberTykk(int iKaik, int iTykk)
    {   int iRet;
        int iUusTop, iUusLeft;
        int iTykkID;
        // Käigud on kellaosuti suunas üleval 1, paremal 2, all 3, vasakul 4
        iRet=-1; // Serv = -1
        if (rl!=null)
        {
            View v=rl.getChildAt(iTykk);

                switch (iKaik)
                {
                    case 1:// ülevalt alla
                        iUusTop=v.getTop()-v.getHeight();
                        iUusLeft=v.getLeft();
                        iRet=getElementatKoords(iUusTop,iUusLeft);
                        break;
                    case 2:// paremalt
                        iUusTop=v.getTop();
                        iUusLeft=v.getLeft()+v.getWidth();
                        iRet=getElementatKoords(iUusTop,iUusLeft);
                        break;
                    case 3:// alt
                        iUusTop=v.getTop()+v.getHeight();
                        iUusLeft=v.getLeft();
                        iRet=getElementatKoords(iUusTop,iUusLeft);
                        break;
                    case 4:// vasakult
                        iUusTop=v.getTop();
                        iUusLeft=v.getLeft()-v.getWidth();
                        iRet=getElementatKoords(iUusTop,iUusLeft);
                        break;

                }

        }
        return iRet;
    }

    private int getViewIDbygetID(int id)
    {  // otsib id välja järgi rl-is oleva View
        int iRet=-1;
        for (int i=0;i<rl.getChildCount();i++)
        {
            View v=rl.getChildAt(i);
            if (v.getId()==id)
            {
                iRet=i;
                break;
            }
        }
    return iRet;
    }
    private int getElementatKoords(int iUusTop, int iUusLeft) {
        int iRet=-1;
            for (int i=0;i<rl.getChildCount();i++)
            {
                View v=rl.getChildAt(i);

                    if (v.getLeft()==iUusLeft && v.getTop()==iUusTop)
                    {
                        iRet=i;
                        break;
                    }

            }
        return iRet;
    }

    private void doPaneTyhiTykkTagasi()
    {
        int iTykkid;
        // Juba on UI threadis.
        for (int i=0;i<rl.getChildCount();i++)
        {
            View v=rl.getChildAt(i);
            if (v.getVisibility()==View.GONE)
            {
                v.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                iTykkid=getLeiaAlgAsukohtID(v.getId());
                if (iTykkid!=-1)
                {
                    lParams.leftMargin=tykid.get(iTykkid).getX();
                    lParams.topMargin=tykid.get(iTykkid).getY();
                }

                v.setLayoutParams(lParams);
                v.invalidate();
                rl.invalidate();
                break;
            }

        }
    }
    private void doLopetaMang() {
        doPaneTyhiTykkTagasi();
        bMangKaib=false;
    }

    private int getLeiaAlgAsukohtID(int id) {
        int iRet;

        // leiab pildi algasukoha leidmiseks tema ID
        iRet=-1;
        for (int i=0;i<tykid.size();i++)
        {
            if (tykid.get(i).getId()==id)
            {
                // leidsime õige tüki
                iRet=i;
                break;
            }
        }

        return iRet;
    }
}
