package tw.dp103g4.user;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;

import static android.content.Context.MODE_PRIVATE;

public class UserFragment extends Fragment {
    private Activity activity;
    private ListView userItem;
    private ListAdapter listAdapter;
    private boolean login = true;
    private SharedPreferences pref;
    private int memId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        memId = pref.getInt("id", 0);
        login = memId != 0;

        final int[] actionGuest = {R.id.action_userFragment_to_loginFragment};
        final int[] actionUser = {0, R.id.action_userFragment_to_userDetailFragment, R.id.action_userFragment_to_userPasswordFragment, R.id.action_userFragment2_to_myPartyFragment};
        userItem = view.findViewById(R.id.userItem);
        String[] itemArray = getResources().getStringArray(login ? R.array.itemUser : R.array.itemGuest);
        listAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, itemArray);

        userItem.setAdapter(listAdapter);

        userItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              //判斷是否登入
                int[] action = login ? actionUser : actionGuest;
                if (login) {
                    if (position == (userItem.getAdapter().getCount() - 1)) {
                        //logout
                        pref.edit().putString("account", null)
                                .putString("password", null)
                                .putInt("id", 0).apply();
                        Navigation.findNavController(view).popBackStack(R.id.partyFragment, false);
                    } else if (position != 0) {
                        Navigation.findNavController(view).navigate(actionUser[position]);
                    }
                } else {
                    Navigation.findNavController(view).navigate(action[position]);
                }

            }
        });




    }
}
