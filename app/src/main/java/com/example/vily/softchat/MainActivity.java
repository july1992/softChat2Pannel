package com.example.vily.softchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mRoot;
    private static final String TAG = "MainActivity";
    private RecyclerView mRv_recycle;
    private EditText mEt_input;
    private LinearLayout mLly_all;
    private LinearLayout mLly_content;


    private boolean isSoftOpen = false;
    private boolean isPannelOpen = false;


    private int mSoftHeight = 0;
    private ImageView mBtn_emoji;
    private ImageView mBtn_more;
    private TranslateAnimation mAnimation;
    private TestAdapter mTestAdapter;

    private boolean isVisble=true;  // 当第一条和最后一条可见的时候  不移动软键盘
    private boolean otheClose=true;  // 不是右上角点击的关闭


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mRoot = findViewById(R.id.root);
        mRv_recycle = findViewById(R.id.rv_recycle);
        mEt_input = findViewById(R.id.et_input);
        mLly_all = findViewById(R.id.lly_all);
        mLly_content = findViewById(R.id.lly_content);
        mBtn_emoji = findViewById(R.id.btn_emoji);
        mBtn_more = findViewById(R.id.btn_more);

        initData();


        initListenet();

        isSoftOpen = false;
        isPannelOpen = false;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListenet() {

        mRv_recycle.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                Log.i(TAG, "onScrollStateChanged: -----:");
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;

                    int lastItemPosition = linearManager.findLastVisibleItemPosition();

                    int firstItemPosition = linearManager.findFirstVisibleItemPosition();

                    if(firstItemPosition==0 && lastItemPosition==mTestAdapter.getData().size()-1){
                        isVisble=true;
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        new HeightProvider(this).init().setHeightListener(new HeightProvider.HeightListener() {
            @Override
            public void onHeightChanged(final int height) {
                Log.i(TAG, "onHeightChanged: ---:" + height);
                TranslateAnimation animation = null;
                Log.i(TAG, "onHeightChanged: ------:"+mLly_all.getBottom());
                Log.i(TAG, "onHeightChanged: ------:"+mLly_all.getY());
                if (height > 0) {
                    isSoftOpen = true;
                    mSoftHeight = height;

                    // 从当天位置移动到指定位置

                    animation = initAnimation(300, -mSoftHeight);



                } else {
                    isSoftOpen = false;
                    animation = initAnimation(300, mSoftHeight);

                }

//                if(!otheClose){   // 如果不是其他地方关闭的软键盘  那就是按钮关闭的软键盘
//
//                    if(isPannelOpen){
//                        mLly_content.startAnimation(animation);
//                    }
//                    isPannelOpen=false;
//
//                }

                if (!isPannelOpen) {   // 如果聊天面板不可见
                    Log.i(TAG, "onHeightChanged: ----;呜呜呜呜");
                    mLly_all.startAnimation(animation);
                }


                animation.setAnimationListener(new SimpleAnimationListener() {

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        if (!isPannelOpen) {
                            if (height > 0) {
                                mLly_all.clearAnimation();
                                mLly_all.setY(-height);
                                isPannelOpen=false;
//                                mLly_content.setY(mRoot.getHeight());
                            } else {
                                mLly_all.clearAnimation();
                                mLly_all.setY(height);
                            }
//                            mLly_content.clearAnimation();
                        }
                    }
                });

                otheClose=false;

            }
        });

        mBtn_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBtn_emoji.setSelected(!mBtn_emoji.isSelected());


                if(mBtn_more.isSelected()){
                    mBtn_more.setSelected(false);
                    mBtn_emoji.setSelected(true);
                }

                Log.i(TAG, "onClick: ----:"+mBtn_emoji.isSelected()+"---:"+mBtn_more.isSelected());
                switchPannel(mBtn_emoji);
            }


        });

        mBtn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBtn_more.setSelected(!mBtn_more.isSelected());

                if(mBtn_emoji.isSelected()){
                    mBtn_emoji.setSelected(false);
                    mBtn_more.setSelected(true);
                }


                Log.i(TAG, "onClick: ----:"+mBtn_emoji.isSelected()+"---:"+mBtn_emoji.isSelected());


                switchPannel(mBtn_more);
            }
        });
        mRv_recycle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "onClick: -------1");

                if (event.getAction() == MotionEvent.ACTION_DOWN) {


                    mEt_input.clearFocus();

                    TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mLly_content.getHeight());
                    animation.setDuration(300);
                    animation.setFillAfter(true);


                    if (isPannelOpen) {
                        if (isSoftOpen) {
                            otheClose=true;
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mEt_input.getWindowToken(), 0);
                        }

                        Log.i(TAG, "onTouch: -----:内容开始下降");
                        mLly_content.startAnimation(animation);
                        mLly_all.startAnimation(animation);

                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mLly_content.clearAnimation();
                                mLly_content.setY(mRoot.getHeight());

                                mLly_all.clearAnimation();
                                mLly_all.setY(0);

                                isPannelOpen = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    } else {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mEt_input.getWindowToken(), 0);
                    }
                    isSoftOpen = false;
                    mBtn_emoji.setSelected(false);
                    mBtn_more.setSelected(false);
                }
                return false;
            }
        });

    }

    private void switchPannel(View view) {

        if(view.isSelected()){  // 显示面板

            Log.i(TAG, "onClick: -----1");
            TranslateAnimation animation=initAnimation(300,-mLly_content.getHeight());
            if (isSoftOpen) {
                otheClose=true;
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEt_input.getWindowToken(), 0);
            }else{
                if(!isPannelOpen){
                    mLly_all.startAnimation(animation);
                }
            }
            if(!isPannelOpen){
                mLly_content.startAnimation(animation);
            }

            animation.setAnimationListener(new SimpleAnimationListener(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    isPannelOpen=true;
                    mLly_content.clearAnimation();
                    mLly_content.setY(mRoot.getHeight() - mLly_content.getHeight());
                    if (!isSoftOpen) {
                        mLly_all.clearAnimation();
                        mLly_all.setY(-mLly_content.getHeight());
                    }
                }
            });


        }else{   // 显示键盘
            Log.i(TAG, "onClick: -------2");
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            mEt_input.requestFocus();
            imm.showSoftInput(mEt_input, 0);
        }

    }

    public TranslateAnimation initAnimation(int duration, float height) {
        mAnimation = new TranslateAnimation(0, 0, 0, height);
        mAnimation.setDuration(duration);
        mAnimation.setFillAfter(true);
        return mAnimation;

    }

    private void initData() {

        mRv_recycle.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            data.add("ssss:" + i);
        }
        mTestAdapter = new TestAdapter(data);
        mRv_recycle.setAdapter(mTestAdapter);


    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}
