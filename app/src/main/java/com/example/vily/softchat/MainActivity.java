package com.example.vily.softchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private TextView mTv_content;
    private HeightProvider mHeightProvider;
    private LinearLayout mLly_input;


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

        mTv_content = findViewById(R.id.tv_content);
        mLly_input = findViewById(R.id.lly_input);

        initData();


        isVisble=true;
        isSoftOpen = false;
        isPannelOpen = false;


        initListenet();


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

                    }else{
                        isVisble=false;
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mHeightProvider = new HeightProvider(this).init();
        mHeightProvider.setHeightListener(new HeightProvider.HeightListener() {
            @Override
            public void onHeightChanged(final int height) {
                Log.i(TAG, "onHeightChanged: ---:" + height+"------isVisble:"+isVisble);
                TranslateAnimation animation = null;

                if (height > 0) {
                    isSoftOpen = true;
                    mSoftHeight = height;
                } else {
                    isSoftOpen = false;
                }

                final int move = (int) (mLly_content.getY() - (mRoot.getHeight() - height));
                animation = initAnimation(300, -move);

                Log.i(TAG, "onHeightChanged: ----move:" + move);

                if (!isPannelOpen) {   // 如果聊天面板不可见
                    mLly_all.startAnimation(animation);
                    mLly_content.startAnimation(animation);
                }else if(height>0 ){

                    mLly_all.startAnimation(animation);
                    mLly_content.startAnimation(animation);
                }

                animation.setAnimationListener(new SimpleAnimationListener() {

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        if (height > 0) {
                            mLly_all.clearAnimation();
                            mLly_all.setY(-height);


                            mLly_content.clearAnimation();
                            mLly_content.setY(mRoot.getHeight()-height);

                            isPannelOpen=false;   // 软键盘出现的时候  聊天面板不可见
                        }else{
                            if(!isPannelOpen){
                                mLly_all.clearAnimation();
                                mLly_all.setY(height);

                                mLly_content.clearAnimation();
                                mLly_content.setY(mRoot.getHeight());
                            }
                        }


                    }
                });



            }
        });

        mBtn_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPannelOpen=true;
                mBtn_emoji.setSelected(!mBtn_emoji.isSelected());

                mTv_content.setText("sssssssssssssssss");
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
                isPannelOpen=true;
                mBtn_more.setSelected(!mBtn_more.isSelected());
                mTv_content.setText("MMMMMMMMMMMMMMMMMMM");
                if(mBtn_emoji.isSelected()){
                    mBtn_emoji.setSelected(false);
                    mBtn_more.setSelected(true);
                }


                Log.i(TAG, "onClick: ----:"+mBtn_emoji.isSelected()+"---:"+mBtn_emoji.isSelected());


                switchPannel(mBtn_more);
            }
        });

        mEt_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtn_emoji.setSelected(false);
                mBtn_more.setSelected(false);
            }
        });

        mRv_recycle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mEt_input.clearFocus();

                    TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mLly_content.getHeight());
                    animation.setDuration(300);
                    animation.setFillAfter(true);

                    if (isPannelOpen) {
                        if (isSoftOpen) {
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


                    isSoftOpen=false;
                    mBtn_emoji.setSelected(false);
                    mBtn_more.setSelected(false);
                }
                return false;
            }
        });

    }

    private void switchPannel(View view) {

        if(view.isSelected()){  // 显示面板

            Log.i(TAG, "onClick: -----:"+isSoftOpen+"---:"+isPannelOpen);
            TranslateAnimation animation=initAnimation(300,(mRoot.getHeight()-mLly_content.getY())-mLly_content.getHeight());
            if (isSoftOpen) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEt_input.getWindowToken(), 0);
            }
            mLly_all.startAnimation(animation);
            mLly_content.startAnimation(animation);

            animation.setAnimationListener(new SimpleAnimationListener(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
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
        for (int i = 0; i < 3; i++) {
            data.add("ssss:" + i);
        }
        mTestAdapter = new TestAdapter(data);
        mRv_recycle.setAdapter(mTestAdapter);


    }

    public float dpToPx( float valueInDp) {
        DisplayMetrics metrics =getApplicationContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}
