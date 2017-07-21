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

package com.lyloou.test.gank;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lyloou.test.R;
import com.lyloou.test.common.NetWork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class ActiveDayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private List<String> mList;
    private boolean mMaxed;
    private Context mContext;
    private OnItemClickListener mItemClickListener;
    private String mTitle;

    ActiveDayAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == TYPE_ITEM) {
            View view = inflater.inflate(R.layout.item_gank_welfare, parent, false);
            return new ActiveDayHolder(view);
        } else if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_gank_header, parent, false);
            return new HeaderHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = inflater.inflate(R.layout.item_gank_footer, parent, false);
            return new FooterHolder(view);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " make sure you using types correctly.");
    }

    public boolean isMaxed() {
        return mMaxed;
    }

    public void setMaxed(boolean maxed) {
        mMaxed = maxed;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ActiveDayHolder) {
            ActiveDayHolder holder = (ActiveDayHolder) viewHolder;
            String activeDay = mList.get(position - 1); // 注意需要减去header的数量

            holder.tvItem.setText(activeDay);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemClickListener.onClick(view , activeDay);
                }
            });


            String[] split = activeDay.split("-");
            if (split.length == 3) {
                String year = split[0];
                String month = split[1];
                String day = split[2];

                Call<ResponseBody> gankData = NetWork.getGankApi().getGankData(year, month, day);
                gankData.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                ResponseBody body = response.body();
                                if(body==null){
                                    return;
                                }
                                String string = body.string();
                                JSONObject jsonObject = new JSONObject(string);
                                JSONObject results = jsonObject.getJSONObject("results");
                                JSONArray welfares = results.getJSONArray("福利");
                                JSONObject welfare = welfares.getJSONObject(0);
                                System.out.println(welfare);
                                String welfareUrl = welfare.getString("url");
                                Glide.with(mContext.getApplicationContext()).load(welfareUrl).asBitmap().centerCrop().into(holder.ivPic);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });


            } else {
                Toast.makeText(mContext, "格式不对：" + activeDay, Toast.LENGTH_SHORT).show();
            }

        } else if (viewHolder instanceof HeaderHolder) {
            HeaderHolder holder = (HeaderHolder) viewHolder;
            if (!TextUtils.isEmpty(mTitle)) {
                holder.tvHeader.setText(mTitle);
            }
        } else if (viewHolder instanceof FooterHolder) {
            FooterHolder holder = (FooterHolder) viewHolder;
            if (mMaxed) {
                holder.tvFooter.setText("----- 我是有底线的 -----");
            } else {
                holder.tvFooter.setText("加载中...");
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        if (position > getListSize()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return getListSize() + 2;
    }

    public void clearAll() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<String> activeDays) {
        mList.addAll(activeDays);
        notifyDataSetChanged();
    }

    public int getListSize() {
        return mList.size();
    }

    public int getItemTypeCount() {
        return 3;
    }

    interface OnItemClickListener {
        void onClick(View view, String activeDay);
    }

    private static class ActiveDayHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tvItem;
        ImageView ivPic;

        ActiveDayHolder(View itemView) {
            super(itemView);
            view = itemView;
            tvItem = (TextView) view.findViewById(R.id.tv_item);
            ivPic = view.findViewById(R.id.iv_tupian);
        }
    }

    private static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        HeaderHolder(View view) {
            super(view);
            tvHeader = view.findViewById(R.id.tv_header);
        }
    }

    private static class FooterHolder extends RecyclerView.ViewHolder {
        TextView tvFooter;

        FooterHolder(View view) {
            super(view);
            tvFooter = (TextView) view.findViewById(R.id.tv_footer);
        }
    }
}