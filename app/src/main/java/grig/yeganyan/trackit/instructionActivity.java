package grig.yeganyan.trackit;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class instructionActivity extends AppCompatActivity {

    private TextView txtEmoji, txtTitle, txtDesc;
    private View dot1, dot2, dot3, dot4;
    private MaterialButton btnNext, btnBack;
    private LinearLayout cardContentContainer;

    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        txtEmoji = findViewById(R.id.txtInstructionEmoji);
        txtTitle = findViewById(R.id.txtInstructionTitle);
        txtDesc = findViewById(R.id.txtInstructionDesc);
        cardContentContainer = findViewById(R.id.cardContentContainer);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);

        btnNext = findViewById(R.id.btnNextInstruction);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            if (currentPage < 3) {
                currentPage++;
                updatePageData();
            } else {
                finish();
            }
        });
        updatePageData();
    }

    private void updatePageData() {
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        switch (currentPage) {
            case 0:
                txtEmoji.setText("🚀");
                txtTitle.setText(getString(R.string.guide_title_welcome));
                txtDesc.setText(getString(R.string.guide_desc_welcome));
                updateDotIndicator(dot1, dot2, dot3, dot4);
                btnNext.setText(getString(R.string.guide_btn_next));
                break;

            case 1:
                txtEmoji.setText("🔥");
                txtTitle.setText(getString(R.string.guide_title_habits));
                txtDesc.setText(getString(R.string.guide_desc_habits));
                updateDotIndicator(dot2, dot1, dot3, dot4);
                btnNext.setText(getString(R.string.guide_btn_next));
                break;

            case 2:
                txtEmoji.setText("🤖");
                txtTitle.setText(getString(R.string.guide_title_coach));
                txtDesc.setText(getString(R.string.guide_desc_coach));
                updateDotIndicator(dot3, dot2, dot1, dot4);
                btnNext.setText(getString(R.string.guide_btn_next));
                break;

            case 3:
                txtEmoji.setText("🏆");
                txtTitle.setText(getString(R.string.guide_title_leaderboard));
                txtDesc.setText(getString(R.string.guide_desc_leaderboard));
                updateDotIndicator(dot4, dot3, dot2, dot1);
                btnNext.setText(getString(R.string.guide_btn_finish)); // Changes to local "Let's Go!" / "Սկսե՛նք" / "Погнали!"
                break;
        }

        cardContentContainer.startAnimation(slideIn);
        animateElementPop(txtEmoji);
    }

    private void updateDotIndicator(View activeDot, View standard1, View standard2, View standard3) {

        resizeDotWidth(activeDot, 24);
        activeDot.setBackgroundResource(R.drawable.dot_active);


        resizeDotWidth(standard1, 8);
        standard1.setBackgroundResource(R.drawable.dot_inactive);

        resizeDotWidth(standard2, 8);
        standard2.setBackgroundResource(R.drawable.dot_inactive);

        resizeDotWidth(standard3, 8);
        standard3.setBackgroundResource(R.drawable.dot_inactive);
    }

    private void resizeDotWidth(View dot, int dpValue) {
        float density = getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams params = dot.getLayoutParams();
        params.width = (int) (dpValue * density);
        dot.setLayoutParams(params);
    }

    private void animateElementPop(View view) {
        view.setScaleX(0.5f);
        view.setScaleY(0.5f);
        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new android.view.animation.OvershootInterpolator()).start();
    }
}