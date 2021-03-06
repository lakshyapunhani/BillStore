package com.fabuleux.wuntu.billstore.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fabuleux.wuntu.billstore.Adapters.FaqAdapter;
import com.fabuleux.wuntu.billstore.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FaqActivity extends AppCompatActivity
{

    @BindView(R.id.faq_recycler_view)
    RecyclerView faq_recycler_view;

    FaqAdapter faqAdapter;

    String[] questions;

    String[] answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        ButterKnife.bind(this);

        questions = getResources().getStringArray(R.array.questions);
        answers = getResources().getStringArray(R.array.answers);

        faqAdapter = new FaqAdapter(questions,answers);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FaqActivity.this);
        faq_recycler_view.setLayoutManager(linearLayoutManager);
        faq_recycler_view.setAdapter(faqAdapter);
    }

    @OnClick(R.id.back_arrow_faq)
    public void backClick()
    {
        finish();
    }
}
