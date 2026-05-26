package grig.yeganyan.trackit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class HabitFragment extends Fragment {

    private EditText searchInput;
    private List<CardView> habitCards = new ArrayList<>();
    private List<String> habitTitles = new ArrayList<>();

    public HabitFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_habit, container, false);

        CardView card2 = view.findViewById(R.id.habitCard2);
        CardView card3 = view.findViewById(R.id.habitCard3);

        CardView card5 = view.findViewById(R.id.habitCard5);
        CardView card6 = view.findViewById(R.id.habitCard6);
        CardView card7 = view.findViewById(R.id.habitCard7);
        CardView card8 = view.findViewById(R.id.habitCard8);
        CardView card9 = view.findViewById(R.id.habitCard9);
        CardView card10 = view.findViewById(R.id.habitCard10);
        CardView card11 = view.findViewById(R.id.habitCard11);
        CardView card12 = view.findViewById(R.id.habitCard12);
        CardView card13 = view.findViewById(R.id.habitCard13);
        CardView card14 = view.findViewById(R.id.habitCard14);
        CardView card15 = view.findViewById(R.id.habitCard15);
        CardView card16 = view.findViewById(R.id.habitCard16);
        CardView card17 = view.findViewById(R.id.habitCard17);
        CardView card18 = view.findViewById(R.id.habitCard18);
        CardView card19 = view.findViewById(R.id.habitCard19);
        CardView card20 = view.findViewById(R.id.habitCard20);
        CardView card21 = view.findViewById(R.id.habitCard21);
        CardView card22 = view.findViewById(R.id.habitCard22);
        CardView card23 = view.findViewById(R.id.habitCard23);
        CardView card24 = view.findViewById(R.id.habitCard24);
        CardView card25 = view.findViewById(R.id.habitCard25);

        habitCards.add(card2);
        habitCards.add(card3);

        habitCards.add(card5);
        habitCards.add(card6);
        habitCards.add(card7);
        habitCards.add(card8);
        habitCards.add(card9);
        habitCards.add(card10);
        habitCards.add(card11);
        habitCards.add(card12);
        habitCards.add(card13);
        habitCards.add(card14);
        habitCards.add(card15);
        habitCards.add(card16);
        habitCards.add(card17);
        habitCards.add(card18);
        habitCards.add(card19);
        habitCards.add(card20);
        habitCards.add(card21);
        habitCards.add(card22);
        habitCards.add(card23);
        habitCards.add(card24);
        habitCards.add(card25);


        habitTitles.add("drink water");
        habitTitles.add("read book");
        habitTitles.add("evening walk");
        habitTitles.add("morning stretch");
        habitTitles.add("journaling");
        habitTitles.add("daily coding");
        habitTitles.add("sleep early");
        habitTitles.add("healthy snack");
        habitTitles.add("evening reflection");
        habitTitles.add("meditate");
        habitTitles.add("workout");
        habitTitles.add("learn language");
        habitTitles.add("manage budget");
        habitTitles.add("digital detox");
        habitTitles.add("call family");
        habitTitles.add("daily walk");
        habitTitles.add("cook healthy");
        habitTitles.add("clean up");
        habitTitles.add("plan day");
        habitTitles.add("take vitamins");
        habitTitles.add("hobby practice");
        habitTitles.add("express gratitude");
        habitTitles.add("listen podcast");
        habitTitles.add("clear inbox");


        card2.setOnClickListener(v -> openAddHabit("💧", getString(R.string.temp_title_water), getString(R.string.temp_desc_water)));
        card3.setOnClickListener(v -> openAddHabit("📖", getString(R.string.temp_title_read), getString(R.string.temp_desc_read)));
        card5.setOnClickListener(v -> openAddHabit("🤸‍️️", getString(R.string.temp_title_stretch), getString(R.string.temp_desc_stretch)));
        card6.setOnClickListener(v -> openAddHabit("📝", getString(R.string.temp_title_journal), getString(R.string.temp_desc_journal)));
        card7.setOnClickListener(v -> openAddHabit("💻", getString(R.string.temp_title_coding), getString(R.string.temp_desc_coding)));
        card8.setOnClickListener(v -> openAddHabit("🌙", getString(R.string.temp_title_sleep), getString(R.string.temp_desc_sleep)));
        card9.setOnClickListener(v -> openAddHabit("🥗", getString(R.string.temp_title_snack), getString(R.string.temp_desc_snack)));
        card10.setOnClickListener(v -> openAddHabit("🌅", getString(R.string.temp_title_reflection), getString(R.string.temp_desc_reflection)));
        card11.setOnClickListener(v -> openAddHabit("🧘", getString(R.string.temp_title_meditate), getString(R.string.temp_desc_meditate)));
        card12.setOnClickListener(v -> openAddHabit("🏋️", getString(R.string.temp_title_workout), getString(R.string.temp_desc_workout)));
        card13.setOnClickListener(v -> openAddHabit("🗣️", getString(R.string.temp_title_language), getString(R.string.temp_desc_language)));
        card14.setOnClickListener(v -> openAddHabit("💰", getString(R.string.temp_title_budget), getString(R.string.temp_desc_budget)));
        card15.setOnClickListener(v -> openAddHabit("📵", getString(R.string.temp_title_disconnect), getString(R.string.temp_desc_disconnect)));
        card16.setOnClickListener(v -> openAddHabit("📞", getString(R.string.temp_title_family), getString(R.string.temp_desc_family)));
        card17.setOnClickListener(v -> openAddHabit("🚶", getString(R.string.temp_title_walk), getString(R.string.temp_desc_walk)));
        card18.setOnClickListener(v -> openAddHabit("🍳", getString(R.string.temp_title_cook), getString(R.string.temp_desc_cook)));
        card19.setOnClickListener(v -> openAddHabit("🧹", getString(R.string.temp_title_clean), getString(R.string.temp_desc_clean)));
        card20.setOnClickListener(v -> openAddHabit("📅", getString(R.string.temp_title_plan), getString(R.string.temp_desc_plan)));
        card21.setOnClickListener(v -> openAddHabit("💊", getString(R.string.temp_title_vitamins), getString(R.string.temp_desc_vitamins)));
        card22.setOnClickListener(v -> openAddHabit("🎨", getString(R.string.temp_title_hobby), getString(R.string.temp_desc_hobby)));
        card23.setOnClickListener(v -> openAddHabit("🙏", getString(R.string.temp_title_gratitude), getString(R.string.temp_desc_gratitude)));
        card24.setOnClickListener(v -> openAddHabit("🎧", getString(R.string.temp_title_podcast), getString(R.string.temp_desc_podcast)));
        card25.setOnClickListener(v -> openAddHabit("📧", getString(R.string.temp_title_inbox), getString(R.string.temp_desc_inbox)));

        searchInput = view.findViewById(R.id.searchInput);


        searchInput.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                InputMethodManager imm = (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                }

                searchInput.clearFocus();
                return true;
            }

            return false;
        });


        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHabits(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void openAddHabit(String emoji, String title, String desc){
        Intent intent = new Intent(getActivity(), AddHabit.class);
        intent.putExtra("emoji", emoji);
        intent.putExtra("title", title);
        intent.putExtra("desc", desc);
        startActivity(intent);
    }

    private void filterHabits(String text) {

        text = text.toLowerCase();

        for (int i = 0; i < habitCards.size(); i++) {

            CardView card = habitCards.get(i);
            String title = habitTitles.get(i);

            if (title.contains(text)) {
                card.setVisibility(View.VISIBLE);
            } else {
                card.setVisibility(View.GONE);
            }
        }
    }
}