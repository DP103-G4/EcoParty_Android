package tw.dp103g4.main_android;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bozin.partylist_android.R;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView= findViewById(R.id.navigation);
        NavController navController = Navigation.findNavController(this, R.id.nav_party_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        View messageTab = menuView.getChildAt(3);
        BottomNavigationItemView itemView = (BottomNavigationItemView) messageTab;
        View badge = LayoutInflater.from(this).inflate(R.layout.item_badge, menuView, false);
        itemView.addView(badge);
        TextView count = badge.findViewById(R.id.ivBadge);
        count.setText(String.valueOf(1));

        count.setVisibility(View.VISIBLE);
    }


    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    // 重寫onTouchEvent的監聽方法
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (MainActivity.this.getCurrentFocus() != null) {
                if (MainActivity.this.getCurrentFocus().getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return super.onTouchEvent(event);
    }


}

