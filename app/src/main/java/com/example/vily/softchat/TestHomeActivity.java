package com.example.vily.softchat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TestHomeActivity extends AppCompatActivity {

    private RelativeLayout mRoot;
    private static final String TAG = "TestActivity";
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

    private boolean isVisble = true;  // 当第一条和最后一条可见的时候  不移动软键盘
    private boolean otheClose = true;  // 不是右上角点击的关闭
    private TextView mTv_content;
    private LinearLayout mLly_input;
    private int mInputHeight;
    private LinearLayout mLly_pannel;
    private Button mBtn_send;
    private View mView_fill;
    private LinearLayout mLly_toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_home);


        mRoot = findViewById(R.id.root);
        mRv_recycle = findViewById(R.id.rv_recycle);
        mEt_input = findViewById(R.id.et_input);
        mLly_all = findViewById(R.id.lly_all);
        mLly_content = findViewById(R.id.lly_content);
        mBtn_emoji = findViewById(R.id.btn_emoji);
        mBtn_more = findViewById(R.id.btn_more);
        mLly_input = findViewById(R.id.lly_input);

        mTv_content = findViewById(R.id.tv_content);
        mLly_pannel = findViewById(R.id.lly_pannel);
        mBtn_send = findViewById(R.id.btn_send);
        mView_fill = findViewById(R.id.view_fill);
        mLly_toolbar = findViewById(R.id.lly_toolbar);


        initData();


        isVisble=true;
        isSoftOpen = false;
        isPannelOpen = false;

        mRoot.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onCreate: -------");
                Log.i(TAG, "run: ----xxxxxxxxxxx:" + mRoot.getHeight());
                mInputHeight = mEt_input.getHeight();
                mLly_content.setY(mLly_pannel.getHeight());
            }
        });

        initListenet();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListenet() {

        mRoot.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mRv_recycle.smoothScrollToPosition(mTestAdapter.getItemCount()-1);
            }
        });
        mBtn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestAdapter.addData("ssss:"+mTestAdapter.getItemCount());
                mRv_recycle.smoothScrollToPosition(mTestAdapter.getItemCount()-1);
            }
        });
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

                    if (firstItemPosition == 0 && lastItemPosition == mTestAdapter.getData().size() - 1) {
                        isVisble = true;
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
                Log.i(TAG, "onHeightChanged: ---:" + height + "------:" + mLly_all.getY());
                TranslateAnimation animation = null;
                TranslateAnimation animationAll = null;

                int space=mRoot.getHeight()-mLly_toolbar.getHeight()-height-mLly_input.getHeight();
                int moveAll=0;
                if (height > 0) {    //软键盘出现
                    isSoftOpen = true;
                    mSoftHeight = height;
                    otheClose = false;
                    // 判断recycle 是否被遮盖
                    if(mLly_all.getHeight()>space){
                        moveAll= (int) (space-mLly_all.getHeight()-mLly_all.getY());
                    }
                } else {              // 软键盘消失
                    isSoftOpen = false;

                    if(mLly_all.getY()!=0){
                        moveAll= -(int) mLly_all.getY();
                    }
                }


                animationAll = initAnimation(300, moveAll);
                final int move = (int) (mLly_content.getY()  + mView_fill.getHeight() - (mRoot.getHeight() - height-mLly_input.getHeight()));
                animation = initAnimation(200, -move);

                Log.i(TAG, "onHeightChanged: ----move:" + move+"-----moveAll:"+moveAll+"-----space:"+space+"---- mLly_all.getHeight():"+ mLly_all.getHeight());

                if (!isPannelOpen) {   // 如果聊天面板不可见
                    mLly_all.startAnimation(animationAll);
                    mLly_content.startAnimation(animation);
                }else if(height>0 ){
                    mLly_all.startAnimation(animationAll);
                    mLly_content.startAnimation(animation);
                }


                animation.setAnimationListener(new SimpleAnimationListener() {

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);

                        if (height > 0) {
                            mLly_content.clearAnimation();
                            mLly_content.setY(mRoot.getHeight() - height - mLly_input.getHeight()-mView_fill.getHeight());
                            isPannelOpen = false;
                        } else {
                            if(!isPannelOpen){

                                mLly_content.clearAnimation();
                                mLly_content.setY(mLly_pannel.getHeight());
                            }


                        }

                    }
                });

                final int finalMoveAll = moveAll;
                animationAll.setAnimationListener(new SimpleAnimationListener(){
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);

                        if(height>0){
                            mLly_all.clearAnimation();
                            mLly_all.setY(finalMoveAll+mLly_all.getY());
                        }else {
                            if(!isPannelOpen){
                                mLly_all.clearAnimation();
                                mLly_all.setY(0);

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
                if (mBtn_more.isSelected()) {
                    mBtn_more.setSelected(false);
                    mBtn_emoji.setSelected(true);
                }

                Log.i(TAG, "onClick: ----:" + mBtn_emoji.isSelected() + "---:" + mBtn_more.isSelected());
                switchPannel(mBtn_emoji);
            }


        });

        mBtn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPannelOpen=true;
                mBtn_more.setSelected(!mBtn_more.isSelected());
                mTv_content.setText("MMMMMMMMMMMMMMMMMMM");
                if (mBtn_emoji.isSelected()) {
                    mBtn_emoji.setSelected(false);
                    mBtn_more.setSelected(true);
                }


                Log.i(TAG, "onClick: ----:" + mBtn_emoji.isSelected() + "---:" + mBtn_emoji.isSelected());


                switchPannel(mBtn_more);
            }
        });



        mEt_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i(TAG, "onEditorAction: -----mInputHeight:"+mInputHeight+"--:"+mEt_input.getBottom());
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                    //处理事件

                    TranslateAnimation animationAll = null;
                    int space=mView_fill.getHeight()-mLly_toolbar.getHeight();
                    int moveAll=0;
                    if(mLly_all.getHeight()>space){
                        moveAll= (int) (space-mLly_all.getHeight()+mLly_all.getY());
                    }
                    Log.i(TAG, "switchPannel: ----moveAll:"+moveAll);
                    animationAll= initAnimation(300, moveAll);
                    mLly_all.startAnimation(animationAll);
                    animationAll.setAnimationListener(new SimpleAnimationListener(){
                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }
                    });
                }

                return false;

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
                    otheClose = true;
                    mEt_input.clearFocus();

                    TranslateAnimation animation =initAnimation(300,mLly_pannel.getHeight());
                    TranslateAnimation animationAll =initAnimation(300,-mLly_all.getY());
                    if (isPannelOpen) {
                        if (isSoftOpen) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mEt_input.getWindowToken(), 0);
                        }

                        Log.i(TAG, "onTouch: -----:内容开始下降");
                        mLly_content.startAnimation(animation);
                        mLly_all.startAnimation(animationAll);

                        animation.setAnimationListener(new SimpleAnimationListener(){
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                super.onAnimationEnd(animation);
                                mLly_content.clearAnimation();
                                mLly_content.setY(mLly_pannel.getHeight());
                                isPannelOpen = false;
                            }
                        });
                        animationAll.setAnimationListener(new SimpleAnimationListener(){
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                super.onAnimationEnd(animation);
                                mLly_all.clearAnimation();
                                mLly_all.setY(0);
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

        if (view.isSelected()) {  // 显示面板

            mEt_input.clearFocus();
            Log.i(TAG, "onClick: -----:" + isSoftOpen + "---:" + isPannelOpen+"----:"+mLly_all.getY());
            TranslateAnimation animation = initAnimation(300, (-mLly_content.getY()));
            TranslateAnimation animationAll = null;
            int space=mView_fill.getHeight()-mLly_toolbar.getHeight();
            int moveAll=0;
            if(mLly_all.getHeight()>space){
                moveAll= (int) (space-mLly_all.getHeight()-mLly_all.getY());
            }
            Log.i(TAG, "switchPannel: ----moveAll:"+moveAll);
            animationAll= initAnimation(300, moveAll);
            if (isSoftOpen) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEt_input.getWindowToken(), 0);
            }

            mLly_all.startAnimation(animationAll);
            mLly_content.startAnimation(animation);

            final int finalMoveAll = moveAll;
            animation.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);

                    mLly_content.clearAnimation();
                    mLly_content.setY(0);
                }
            });
            animationAll.setAnimationListener(new SimpleAnimationListener(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    if (!isSoftOpen) {
                        mLly_all.clearAnimation();
                        mLly_all.setY(mLly_all.getY()+ finalMoveAll);
                    }
                }
            });

        } else {   // 显示键盘
            Log.i(TAG, "onClick: -------2");
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            mEt_input.requestFocus();
            imm.showSoftInput(mEt_input, 0);
        }

    }

    public TranslateAnimation initAnimation(int duration, float height) {
//        mAnimation = new TranslateAnimation(0, 0, 0, height);
        mAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0,
                TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, height);
        mAnimation.setDuration(duration);
        mAnimation.setFillAfter(true);
        return mAnimation;

    }

    private void initData() {

        mRv_recycle.setLayoutManager(new LinearLayoutManager(TestHomeActivity.this));

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            data.add("ssss:" + i);
        }
        mTestAdapter = new TestAdapter(data);
        mRv_recycle.setAdapter(mTestAdapter);


    }

    public float dpToPx(float valueInDp) {
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}
