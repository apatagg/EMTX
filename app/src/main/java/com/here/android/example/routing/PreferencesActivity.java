package com.here.android.example.routing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.here.android.mpa.search.Category;
import com.here.android.mpa.search.CategoryFilter;

import java.util.Arrays;

public class PreferencesActivity extends AppCompatActivity {
    public static CategoryFilter selectedCategories;

    private static boolean[] checked = new boolean[6];

    static {
        selectedCategories = new CategoryFilter();
        selectedCategories.add(Category.Global.ACCOMMODATION);
        selectedCategories.add(Category.Global.EAT_DRINK);
        selectedCategories.add(Category.Global.GOING_OUT);
        selectedCategories.add(Category.Global.NATURAL_GEOGRAPHICAL);
        selectedCategories.add(Category.Global.SHOPPING);
        selectedCategories.add(Category.Global.SIGHTS_MUSEUMS);
        Arrays.fill(checked, true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        CheckBox c1 = findViewById(R.id.accommodationCheckBox);
        CheckBox c2 = findViewById(R.id.eatDrinkCheckBox);
        CheckBox c3 = findViewById(R.id.goingOutCheckBox);
        CheckBox c4 = findViewById(R.id.naturalGeographicalCheckBox);
        CheckBox c5 = findViewById(R.id.shoppingCheckBox);
        CheckBox c6 = findViewById(R.id.sightsMuseumsCheckBox);

        if (checked[0]) c1.setChecked(true);
        if (checked[1]) c2.setChecked(true);
        if (checked[2]) c3.setChecked(true);
        if (checked[3]) c4.setChecked(true);
        if (checked[4]) c5.setChecked(true);
        if (checked[5]) c6.setChecked(true);
    }


    public void createPreferenceList(View view) {

        CategoryFilter filters = new CategoryFilter();

        CheckBox c1 = findViewById(R.id.accommodationCheckBox);
        CheckBox c2 = findViewById(R.id.eatDrinkCheckBox);
        CheckBox c3 = findViewById(R.id.goingOutCheckBox);
        CheckBox c4 = findViewById(R.id.naturalGeographicalCheckBox);
        CheckBox c5 = findViewById(R.id.shoppingCheckBox);
        CheckBox c6 = findViewById(R.id.sightsMuseumsCheckBox);

        checked[0] = c1.isChecked();
        checked[1] = c2.isChecked();
        checked[2] = c3.isChecked();
        checked[3] = c4.isChecked();
        checked[4] = c5.isChecked();
        checked[5] = c6.isChecked();

        if(c1.isChecked()) { filters.add(Category.Global.ACCOMMODATION); }
        if(c2.isChecked()) { filters.add(Category.Global.EAT_DRINK); }
        if(c3.isChecked()) { filters.add(Category.Global.GOING_OUT); }
        if(c4.isChecked()) { filters.add(Category.Global.NATURAL_GEOGRAPHICAL); }
        if(c5.isChecked()) { filters.add(Category.Global.SHOPPING); }
        if(c6.isChecked()) { filters.add(Category.Global.SIGHTS_MUSEUMS); }

        PreferencesActivity.selectedCategories = filters;
        finish();
    }

}
