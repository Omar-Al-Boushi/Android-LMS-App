package org.svuonline.lms.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.imageview.ShapeableImageView;

import org.svuonline.lms.R;
import org.svuonline.lms.utils.Utils;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    private static final int DURATION = 800; // مدة الانتقال التدريجي
    private static final int TRANSITION_DURATION = 300; // مدة الترانزيشن
    private static final int STAY_DURATION = 100; // مدة التثبيت قبل التغيير

    private ConstraintLayout mainLayout;
    private ShapeableImageView splashLogo;
    private int orderIndex = 0; // مؤشر الترتيب
    private final Handler handler = new Handler();

    private final int[][] transitions = {
            {R.color.white, R.drawable.logo_main},
            {R.color.white, R.drawable.logo_secondary},
            {R.color.Custom_MainColorGolden, R.drawable.logo_white},
            {R.color.Custom_MainColorBlue, R.drawable.logo_white},
    };

    private final Runnable colorChanger = new Runnable() {
        @Override
        public void run() {
            if (orderIndex < transitions.length - 1) {
                int startColor = ContextCompat.getColor(SplashScreenActivity.this, transitions[orderIndex][0]);
                int endColor = ContextCompat.getColor(SplashScreenActivity.this, transitions[orderIndex + 1][0]);

                ObjectAnimator colorAnim = ObjectAnimator.ofObject(
                        mainLayout,
                        "backgroundColor",
                        new ArgbEvaluator(),
                        startColor,
                        endColor
                );
                colorAnim.setDuration(TRANSITION_DURATION);
                colorAnim.start();

                Utils.setSystemBarColor(SplashScreenActivity.this, transitions[orderIndex + 1][0], transitions[orderIndex + 1][0], TRANSITION_DURATION);

                ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(splashLogo, "alpha", 1f, 0f);
                fadeOutAnim.setDuration(TRANSITION_DURATION / 2);
                fadeOutAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {
                        splashLogo.setImageResource(transitions[orderIndex][1]);
                        ObjectAnimator fadeInAnim = ObjectAnimator.ofFloat(splashLogo, "alpha", 0f, 1f);
                        fadeInAnim.setDuration(TRANSITION_DURATION);
                        fadeInAnim.start();
                    }
                });
                fadeOutAnim.start();

                orderIndex++;
                handler.postDelayed(this, DURATION + STAY_DURATION);
            } else {
                int finalColor = ContextCompat.getColor(SplashScreenActivity.this, transitions[orderIndex][0]);
                mainLayout.setBackgroundColor(finalColor);
                Utils.setSystemBarColor(SplashScreenActivity.this, transitions[orderIndex][0], transitions[orderIndex][0], TRANSITION_DURATION);
                splashLogo.setImageResource(transitions[orderIndex][1]);
                Intent intent = new Intent(SplashScreenActivity.this, WelcomeActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mainLayout = findViewById(R.id.main);
        splashLogo = findViewById(R.id.splash_logo);


        // تعيين الشعار والخلفية الأولى في البداية بشكل فوري
        int initialColor = ContextCompat.getColor(SplashScreenActivity.this, transitions[0][0]);
        mainLayout.setBackgroundColor(initialColor);
        Utils.setSystemBarColor(this, transitions[0][0], transitions[0][0], TRANSITION_DURATION);

        splashLogo.setImageResource(transitions[0][1]);

        // بدء الانتقالات التالية بالتأخير المحدد
        handler.postDelayed(colorChanger, DURATION);

    }

}
