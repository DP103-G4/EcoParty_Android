package tw.dp103g4.piece;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tw.dp103g4.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class PieceDetailFragment extends Fragment {


    public PieceDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_piece_detail, container, false);
    }

}
