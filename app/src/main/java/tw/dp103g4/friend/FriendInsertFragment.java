package tw.dp103g4.friend;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bozin.partylist_android.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendInsertFragment extends Fragment {


    public FriendInsertFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_insert, container, false);
    }

}