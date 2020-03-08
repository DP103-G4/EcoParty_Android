package tw.dp103g4.news;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;

import tw.dp103g4.R;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.main_android.MainActivity;
import tw.dp103g4.task.NewsImageTask;

public class NewsFragment extends Fragment {
    private static final String TAG = "TAG_NewsFragment";
    private ImageView ivNews;
    private TextView tvNewsTitle, tvNewsContent, tvNewsDate;
    private MainActivity activity;
    private News news;
    private ScrollView scrollView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.getBottomNavigationView().setVisibility(View.GONE);
        final NavController navController = Navigation.findNavController(view);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });
        ivNews = view.findViewById(R.id.ivNews);
        tvNewsTitle = view.findViewById(R.id.tvNewsTitle);
        tvNewsDate = view.findViewById(R.id.tvNewsDate);
        tvNewsContent = view.findViewById(R.id.tvNewsContent);
        scrollView = view.findViewById(R.id.scrollView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });
        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("newsDetail") == null) {
            Common.showToast(activity, R.string.noNewsDetail);
            navController.popBackStack();
            return;
        }
        news = (News) bundle.getSerializable("newsDetail");
        showNews();
    }

    private void showNews() {
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "NewsServlet";
            int id = news.getId();
            int imageSize = getResources().getDisplayMetrics().widthPixels / 3;
            Bitmap bitmap = null;
            try {
                bitmap = new NewsImageTask(url, id, imageSize).execute().get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (bitmap != null) {
                ivNews.setImageBitmap(bitmap);
            } else {
                ivNews.setImageResource(R.drawable.no_image);
            }
            tvNewsTitle.setText(news.getTitle());
            tvNewsContent.setText(news.getContent());
            String newsDateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(news.getTime());
            tvNewsDate.setText(newsDateString);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.getBottomNavigationView().setVisibility(View.VISIBLE);
    }
}
