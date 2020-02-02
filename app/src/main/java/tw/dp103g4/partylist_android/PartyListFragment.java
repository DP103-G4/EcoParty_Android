package tw.dp103g4.partylist_android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.news.News;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.CoverImageTask;
import tw.dp103g4.task.NewsImageTask;

public class PartyListFragment extends Fragment {
    private static final String TAG = "TAG_PartyList";
    private Activity activity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvParty, rvNews, rvPartyStart;
    private List<Party> parties, partyStart;
    private List<News> news;
    private CommonTask partyGetAllTask, newsGetAllTask;
    private CoverImageTask partyImageTask;
    private int imageSize;
    private NewsImageTask newsImageTask;
    private FloatingActionButton floatingActionButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_party, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        floatingActionButton = view.findViewById(R.id.btAdd);
        SearchView searchView = view.findViewById(R.id.searchView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        rvPartyStart = view.findViewById(R.id.rvPartyStart);
        rvPartyStart.setLayoutManager(new LinearLayoutManager(activity));
        rvParty = view.findViewById(R.id.rvParty);
        rvParty.setLayoutManager(new GridLayoutManager(activity, 2));
        rvNews = view.findViewById(R.id.rvNews);
        rvNews.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
        partyStart = getPartyStart();
        showPartyStart(partyStart);
        parties = getParties();
        showParties(parties);
        news = getNews();
        showNews(news);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(rvNews);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                parties = getParties();
                partyStart = getPartyStart();
                swipeRefreshLayout.setRefreshing(true);
                showParties(parties);
                showPartyStart(partyStart);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_partyFragment_to_partyInsertFragment);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.isEmpty()) {
                    showParties(parties);
                } else {
                    List<Party> searchParties = new ArrayList<>();
                    for (Party party : parties) {
                        if (party.getName().toUpperCase().contains(newText.toUpperCase())) {
                            searchParties.add(party);
                        }
                    }
                    showParties(searchParties);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

        });
    }

    private void showPartyStart(List<Party> partyStart) {
        if (partyStart == null || partyStart.isEmpty()) {
            Common.showToast(activity, R.string.textNoPartiesFound);
        }
        PartyStartAdapter partyStartAdapter = (PartyStartAdapter) rvPartyStart.getAdapter();
        if (partyStartAdapter == null) {
            rvPartyStart.setAdapter(new PartyStartAdapter(activity, partyStart));
        } else {
            partyStartAdapter.setPartyStart(partyStart);
            partyStartAdapter.notifyDataSetChanged();
        }
    }

    private List<Party> getPartyStart() {
        List<Party> partyStart = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getCurrentParty");
            jsonObject.addProperty("state", 3);
            jsonObject.addProperty("participantId", 2);
            String jsonOut = jsonObject.toString();
            partyGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = partyGetAllTask.execute().get();
                Type listType = new TypeToken<List<Party>>() {
                }.getType();
                partyStart = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return partyStart;
}

    private List<News> getNews() {
        List<News> news = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "NewsServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllNews");
            String jsonOut = jsonObject.toString();
            newsGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = newsGetAllTask.execute().get();
                Type listType = new TypeToken<List<News>>() {
                }.getType();
                news = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return news;
    }

    private void showNews(List<News> news) {
        if (news == null || news.isEmpty()) {
            Common.showToast(activity, R.string.textNoNewsFound);
        }
        NewsAdapter newsAdapter = (NewsAdapter) rvNews.getAdapter();
        if (newsAdapter == null) {
            rvNews.setAdapter(new NewsAdapter(activity, news));
        } else {
            newsAdapter.setNews(news);
            newsAdapter.notifyDataSetChanged();
        }
    }

    private List<Party> getParties() {
        List<Party> parties = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getPartyList");
            jsonObject.addProperty("state", 1);
            String jsonOut = jsonObject.toString();
            partyGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = partyGetAllTask.execute().get();
                Type listType = new TypeToken<List<Party>>() {
                }.getType();
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                parties = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return parties;
    }

    private void showParties(List<Party> parties) {
        if (parties == null || parties.isEmpty()) {
            Common.showToast(activity, R.string.textNoPartiesFound);
        }
        PartyAdapter partyAdapter = (PartyAdapter) rvParty.getAdapter();
        if (partyAdapter == null) {
            rvParty.setAdapter(new PartyAdapter(activity, parties));
        } else {
            partyAdapter.setParties(parties);
            partyAdapter.notifyDataSetChanged();
        }
    }

    private class PartyAdapter extends RecyclerView.Adapter<PartyAdapter.PartyViewHolder> {
        private List<Party> parties;
        private LayoutInflater layoutInflater;

        PartyAdapter(Context context, List<Party> parties) {
            layoutInflater = LayoutInflater.from(context);
            this.parties = parties;
            imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        }

        void setParties(List<Party> parties) {
            this.parties = parties;
        }

        @Override
        public int getItemCount() {
            return parties.size();
        }

        private class PartyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivParty, ivUser;
            TextView tvTitle, tvAddress, tvTime;

            public PartyViewHolder(View itemView) {
                super(itemView);
                ivParty = itemView.findViewById(R.id.ivParty);
                ivUser = itemView.findViewById(R.id.ivUser);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvTime = itemView.findViewById(R.id.tvTime);
            }
        }

        @NonNull
        @Override
        public PartyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_party, parent, false);
            return new PartyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PartyViewHolder holder, int position) {
            Party party = parties.get(position);
            String url = Common.URL_SERVER + "PartyServlet";
            final int id = party.getId();
            partyImageTask = new CoverImageTask(url, id, imageSize, holder.ivParty);
            partyImageTask.execute();
            holder.ivUser.setImageResource(R.drawable.ivy);
            holder.tvTitle.setText(party.getName());
            holder.tvAddress.setText(party.getAddress());
            holder.tvTime.setText(new SimpleDateFormat("E M月d日").format(party.getStartTime()));
            //bundle活動詳情

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("partyId", id);
                    Navigation.findNavController(v).navigate(R.id.action_partyFragment_to_partyDetailFragment, bundle);
                }
            });

        }
    }

    private class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
        private List<News> news;
        private LayoutInflater layoutInflater;
        public NewsAdapter(Context context, List<News> news) {
            layoutInflater = LayoutInflater.from(context);
            this.news = news;
            imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        }

        void setNews(List<News> news) {
            this.news = news;
        }

        @Override
        public int getItemCount() {
            return news.size();
        }

        private class NewsViewHolder extends RecyclerView.ViewHolder {
            ImageView ivNews;

            public NewsViewHolder(View itemView) {
                super(itemView);
                ivNews = itemView.findViewById(R.id.ivNews);
            }
        }

        @NonNull
        @Override
        public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_news, parent, false);
            return new NewsViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
            final News newsDetail = news.get(position);
            String url = Common.URL_SERVER + "NewsServlet";
            int id = newsDetail.getId();
            newsImageTask = new NewsImageTask(url, id, imageSize, holder.ivNews);
            newsImageTask.execute();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("newsDetail", newsDetail);
                    Navigation.findNavController(v).navigate(R.id.action_partyFragment_to_newsFragment, bundle);
                }
            });
        }

    }

    private class PartyStartAdapter extends RecyclerView.Adapter<PartyStartAdapter.PartyStartViewHolder> {
        private List<Party> partyStart;
        private LayoutInflater layoutInflater;

        public PartyStartAdapter(Context context, List<Party> partyStart) {
            layoutInflater = LayoutInflater.from(context);
            this.partyStart = partyStart;
            imageSize = getResources().getDisplayMetrics().widthPixels / 2;
        }

        void setPartyStart(List<Party> partyStart) {
            this.partyStart = partyStart;
        }

        @Override
        public int getItemCount() {
            return partyStart.size();
        }

        private class PartyStartViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPartyStart;

            public PartyStartViewHolder(View itemView) {
                super(itemView);
                ivPartyStart = itemView.findViewById(R.id.ivPartyStart);
            }
        }

        @NonNull
        @Override
        public PartyStartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_party_start, parent, false);
            return new PartyStartViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PartyStartViewHolder holder, int position) {
            Party mPartyStart = partyStart.get(position);
            String url = Common.URL_SERVER + "PartyServlet";
            int id = mPartyStart.getId();
            partyImageTask = new CoverImageTask(url, id, imageSize, holder.ivPartyStart);
            partyImageTask.execute();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (partyGetAllTask != null) {
            partyGetAllTask.cancel(true);
            partyGetAllTask = null;
        }
        if (partyImageTask != null) {
            partyImageTask.cancel(true);
            partyImageTask = null;
        }
        if (newsImageTask != null) {
            newsImageTask.cancel(true);
            newsImageTask = null;
        }
    }

}
