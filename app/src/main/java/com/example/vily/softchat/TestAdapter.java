package com.example.vily.softchat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class TestAdapter extends BaseQuickAdapter<String,BaseViewHolder> {
    public TestAdapter(@Nullable List<String> data) {
        super(R.layout.adapter_item,data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, String item) {

        helper.setText(R.id.tv_content,item);
    }
}
