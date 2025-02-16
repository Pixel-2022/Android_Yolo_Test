package org.tensorflow.demo;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Fragment_WordCard extends Fragment {

    //백
    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = LoginActivity.getBASE_URL();
    private static RecyclerView recyclerView;

    String[] words;
    Boolean[] stars;
    int[] ids;
    int[] userids;
    String[] images;
    String[] urls;
    private int p_userId = MainActivity.p_userID;
    private String stringp_userId = String.valueOf(p_userId);
    //
    private View v;
    static WordCardAdapter adapter;
    private static RecyclerView.LayoutManager mLayoutManager;
    static ArrayList<Data> dataList = new ArrayList();

    EditText searchET;
    ArrayList<Data> filteredList;
    private static ArrayList<Data> delList = new ArrayList<Data>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.f2_wordcard, container, false);
        Context context = v.getContext();
        recyclerView = v.findViewById(R.id.cardList);
        filteredList = new ArrayList<>();
        searchET = v.findViewById(R.id.search_edit2);

        mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new WordCardAdapter(context, dataList);
        recyclerView.setAdapter(adapter);

        delList.clear();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);
        HashMap<String, String> map = new HashMap<>();
        map.put("UserId", stringp_userId);

        Call<JsonElement> call = retrofitInterface.getListAll(map);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 201) {
                    Log.e("워드카드", "제대로 들어왔읍니다");
                }
                if (response.body() != null) {
                    JsonArray ListResponseArray = response.body().getAsJsonArray();

                    System.out.print(ListResponseArray);

                    ids = new int[ListResponseArray.size()];
                    stars = new Boolean[ListResponseArray.size()];
                    userids = new int[ListResponseArray.size()];
                    words = new String[ListResponseArray.size()];
                    images = new String[ListResponseArray.size()];
                    urls = new String[ListResponseArray.size()];


                    for (int i = 0; i < ListResponseArray.size(); i++) {
                        JsonElement jsonElement = ListResponseArray.get(i);
                        JsonElement jsonElement1 = jsonElement.getAsJsonObject().get("WordDict");
                        String wordImg = jsonElement1.getAsJsonObject().get("wordImg").getAsString();
                        String viurl = jsonElement1.getAsJsonObject().get("videoURL").getAsString();
                        int id = jsonElement.getAsJsonObject().get("id").getAsInt();
                        Boolean star = jsonElement.getAsJsonObject().get("star").getAsBoolean();
                        int uid = jsonElement.getAsJsonObject().get("UserId").getAsInt();
                        String word = jsonElement.getAsJsonObject().get("Word").getAsString();

                        ids[i] = id;
                        stars[i] = star;
                        userids[i] = uid;
                        words[i] = word;
                        images[i] = wordImg;
                        urls[i] = viurl;
                    }
                    //userid 같은 것 들만 리사이클러에 추가
                    dataList.clear();
                    for (int i = 0; i < ListResponseArray.size(); i++) {
                        if (userids[i] == p_userId) {
                            dataList.add(new Data(ids[i], userids[i], stars[i], words[i], images[i], urls[i]));
                        }
                    }
                    System.out.println(dataList);
                    adapter = new WordCardAdapter(context, dataList);
                    mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setAdapter(adapter);

                } else {
                    Log.e("AAA", "내용물이 비어있습니다.");
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                dataList.add(new Data(2, p_userId, false, "예시", "https://drive.google.com/file/d/1dlslbcqaQGUkmIB1FT65uOVrWOkFE89p/view?usp=sharing", "http://drive.google.com/uc?export=view&id=1Fps5iXNIyEbDsfsGc_PVOURrxr63SRZU"));
                adapter = new WordCardAdapter(context, dataList);
                mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(adapter);
            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = searchET.getText().toString();
                searchFilter(searchText);
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void searchFilter(String searchText) {
        filteredList.clear();
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getWord().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(dataList.get(i));
            }
        }
        adapter.filterList(filteredList);
    }

    public static void delFilter(String a) {
        delList.clear();

        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getWord().toLowerCase() != a) {
                delList.add(dataList.get(i));
            }
        }
        adapter.refresh1(delList);
    }
}
