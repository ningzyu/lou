/*
 * Copyright  (c) 2017 Lyloou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lyloou.test.onearticle;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lyloou.test.R;
import com.lyloou.test.common.LouDialog;
import com.lyloou.test.common.NetWork;
import com.lyloou.test.util.Uscreen;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class OneArticleActivity extends AppCompatActivity {

    Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_onearticle);

        initView();

        layoutIt(NetWork.getOneArticleApi().getOneArticle(1));
    }


    private void layoutIt(Observable<OneArticle> observable) {
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<OneArticle>() {
                               @Override
                               public void accept(@NonNull OneArticle oneArticle) throws Exception {
                                   String title = oneArticle.getData().getTitle();
                                   String authDate = oneArticle.getData().getAuthor() + "（" + oneArticle.getData().getDate().getCurr() + "）";
                                   Spanned content = Html.fromHtml(oneArticle.getData().getContent());

                                   showArticle(title, authDate, content);
                               }
                           }
                        , new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                Toast.makeText(mContext, "加载失败：" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void showArticle(String title, String authDate, Spanned content) {
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvAuthorDate = findViewById(R.id.tv_author_date);
        TextView tvContent = findViewById(R.id.tv_content);
        tvTitle.setText(title);
        tvAuthorDate.setText(authDate);
        tvContent.setText(content);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_onearticle);
        toolbar.setTitle("每日一文");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.back_white);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Uscreen.setToolbarMarginTop(mContext, toolbar);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.coolapsing_toolbar_layout_onearticle);
        collapsingToolbarLayout.setExpandedTitleColor(Color.YELLOW);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        ImageView ivHeader = collapsingToolbarLayout.findViewById(R.id.iv_header);

        int image = (int) (98 * Math.random() + 1);
        String url = "https://meiriyiwen.com/images/new_feed/bg_" + image + ".jpg";
        Glide.with(mContext).load(url).into(ivHeader);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_one_artical, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Observable<OneArticle> observable = null;
        switch (item.getItemId()) {
            case R.id.menu_one_article_today:
                observable = NetWork.getOneArticleApi().getOneArticle(1);
                layoutIt(observable);
                break;
            case R.id.menu_one_article_random:
                observable = NetWork.getOneArticleApi().getRandomArticle(1);
                layoutIt(observable);
                break;
            case R.id.menu_one_article_select:
                showSpecialDayArticle();
                break;
            case R.id.menu_one_article_here:
                showSpecialDayArticleHere();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void showSpecialDayArticle() {
        LouDialog louDialog = LouDialog
                .newInstance(mContext, R.layout.dialog_onearticle, R.style.Theme_AppCompat_Dialog)
                .setCancelable(true);
        DatePicker datePicker = louDialog.getView(R.id.dp_one_article);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Log.d("Date", "Year=" + year + " Month=" + (month + 1) + " day=" + dayOfMonth);
                System.out.printf("当前时间：%1$TY-%1$Tm-%1$Td %1$TH:%1$TM:%1$TS", new Date());
                String y = String.format(Locale.getDefault(), "%04d", year);
                String m = String.format(Locale.getDefault(), "%02d", month + 1);
                String d = String.format(Locale.getDefault(), "%02d", dayOfMonth);
                String date = y + m + d;
                Observable<OneArticle> observable = NetWork.getOneArticleApi().getSpecialArticle(1, date);
                layoutIt(observable);
                louDialog.dismiss();
            }
        });

        louDialog.show();
    }

    private void showSpecialDayArticleHere() {
        LouDialog louDialog = LouDialog
                .newInstance(mContext, R.layout.dialog_onearticle_here, R.style.Theme_AppCompat_Dialog)
                .setCancelable(true);
        EditText datePicker = louDialog.getView(R.id.et_one_article_here);
        Button btn = louDialog.getView(R.id.btn_one_article_here);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = datePicker.getText().toString();
                if (!TextUtils.isEmpty(date.trim())) {
                    Observable<OneArticle> observable = NetWork.getOneArticleApi().getSpecialArticle(1, date);
                    layoutIt(observable);
                }
                louDialog.dismiss();
            }
        });

        louDialog.show();
    }
}
