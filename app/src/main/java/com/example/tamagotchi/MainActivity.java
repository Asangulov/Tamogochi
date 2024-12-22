package com.example.tamagotchi;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView creatureImage;
    private TextView stateText, scoreText;
    private ImageView hungerImage, sleepImage, happinessImage, energyImage;
    private int hunger = 50, sleepiness = 50, happiness = 50, energy = 100, score = 0;
    private boolean isSleeping = false;
    private int gameSpeed = 5000;

    private Handler gameHandler = new Handler();
    private Runnable gameLoop;

    private Handler hungerHandler = new Handler();
    private Runnable hungerDecrementRunnable;

    private Handler energyHandler = new Handler();
    private Runnable energyDecrementRunnable;

    private Handler sleepHandler = new Handler();
    private Runnable sleepChangeRunnable;

    private Handler happinessHandler = new Handler();
    private Runnable happinessDecrementRunnable;

    private Button feedButton, sleepButton, playButton, restartButton;

    private String lastFoodType = null;
    private int foodCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Initialization
        creatureImage = findViewById(R.id.creatureImage);
        stateText = findViewById(R.id.stateText);
        scoreText = findViewById(R.id.scoreText);
        hungerImage = findViewById(R.id.hungerImage);
        sleepImage = findViewById(R.id.sleepImage);
        happinessImage = findViewById(R.id.happinessImage);
        energyImage = findViewById(R.id.energyImage);

        feedButton = findViewById(R.id.feedButton);
        sleepButton = findViewById(R.id.sleepButton);
        playButton = findViewById(R.id.playButton);
        restartButton = findViewById(R.id.restartButton);

        stateText.setTextSize(12);
        updateUI();

        // Game Loop
        gameLoop = () -> {
            updateState();
            gameHandler.postDelayed(gameLoop, gameSpeed);
        };
        gameHandler.post(gameLoop);

        // Hunger Decrement
        hungerDecrementRunnable = () -> {
            if (hunger > 0) {
                hunger = Math.max(0, hunger - 1);
                hungerImage.setImageResource(getStateImage(hunger));
                updateStateText();

                if (hunger == 0) {
                    endGame("Pikachu is too hungry.");
                }
            }
            hungerHandler.postDelayed(hungerDecrementRunnable, 2000);
        };
        hungerHandler.post(hungerDecrementRunnable);

        // Energy Decrement
        energyDecrementRunnable = () -> {
            if (!isSleeping && energy > 0) {
                energy = Math.max(0, energy - 1);
                energyImage.setImageResource(getEnergyStateImage(energy));
                updateStateText();
            }
            energyHandler.postDelayed(energyDecrementRunnable, 10000);
        };
        energyHandler.post(energyDecrementRunnable);

        // Sleep Change Handler
        sleepChangeRunnable = () -> {
            if (isSleeping) {
                sleepiness = Math.min(100, sleepiness + 2);
                if (sleepiness == 100) {
                    toggleSleep();
                    Toast.makeText(this, "Pikachu is fully rested!", Toast.LENGTH_SHORT).show();
                    score += 10; // Reward for good sleep
                }
            } else {
                sleepiness = Math.max(0, sleepiness - 1);
                if (sleepiness == 0) {
                    endGame("Pikachu is too tired.");
                }
            }
            updateStateText();
            sleepHandler.postDelayed(sleepChangeRunnable, 2000);
        };
        sleepHandler.post(sleepChangeRunnable);

        // Happiness Decrement (1% per second)
        happinessDecrementRunnable = () -> {
            if (!isSleeping) {
                happiness = Math.max(0, happiness - 1);
                happinessImage.setImageResource(getStateImage(happiness));
                updateStateText();

                // Check if happiness reaches 0
                if (happiness == 0) {
                    endGame("Pikachu is too sad.");
                }
            }
            happinessHandler.postDelayed(happinessDecrementRunnable, 1000);
        };
        happinessHandler.post(happinessDecrementRunnable);

        // Button Listeners
        feedButton.setOnClickListener(view -> {
            if (!isSleeping) showFoodSelectionDialog();
            else Toast.makeText(this, "Pikachu is sleeping!", Toast.LENGTH_SHORT).show();
        });

        sleepButton.setOnClickListener(view -> toggleSleep());

        playButton.setOnClickListener(view -> {
            if (!isSleeping) playWithCreature();
            else Toast.makeText(this, "Pikachu is sleeping!", Toast.LENGTH_SHORT).show();
        });

        restartButton.setOnClickListener(view -> restartGame());
    }

    private void playWithCreature() {
    }

    private void updateUI() {
        hungerImage.setImageResource(getStateImage(hunger));
        energyImage.setImageResource(getEnergyStateImage(energy));
        happinessImage.setImageResource(getStateImage(happiness));
        sleepImage.setImageResource(getStateImage(sleepiness));
        updateStateText();
    }

    private void toggleSleep() {
        isSleeping = !isSleeping;
        if (isSleeping) {
            creatureImage.setImageResource(R.drawable.pikachu_sleeping);
            sleepImage.setImageResource(getStateImage(sleepiness)); // Update sleep state
            feedButton.setEnabled(false);
            playButton.setEnabled(false);
            sleepButton.setText("Wake Up");
        } else {
            creatureImage.setImageResource(R.drawable.pikachu_standing);
            sleepImage.setImageResource(getStateImage(sleepiness)); // Update sleep state
            feedButton.setEnabled(true);
            playButton.setEnabled(true);
            sleepButton.setText("Sleep");
        }
        updateStateText();
    }

    private void updateState() {
        hungerImage.setImageResource(getStateImage(hunger));
        energyImage.setImageResource(getEnergyStateImage(energy));
        happinessImage.setImageResource(getStateImage(happiness));
        sleepImage.setImageResource(getStateImage(sleepiness)); // Update sleep state
        updateStateText();
    }

    private void updateStateText() {
        stateText.setText("Hunger: " + hunger + "% | Sleep: " + sleepiness + "% | Happiness: " + happiness + "% | Energy: " + energy + "%");
        scoreText.setText("Score: " + score);
    }

    private int getStateImage(int value) {
        if (value <= 10) {
            return R.drawable.battery_empty; // 0 bars
        } else if (value <= 30) {
            return R.drawable.battery_half; // 2 bars
        } else if (value <= 50) {
            return R.drawable.battery_three_quarter; // 4 bars
        } else if (value <= 70) {
            return R.drawable.battery_full; // 6 bars
        } else {
            return R.drawable.battery_full; // 7 bars
        }
    }

    private int getEnergyStateImage(int value) {
        if (value <= 20) {
            return R.drawable.battery_empty;
        } else if (value <= 40) {
            return R.drawable.battery_half;
        } else if (value <= 60) {
            return R.drawable.battery_three_quarter;
        } else if (value <= 80) {
            return R.drawable.battery_nefull;
        } else {
            return R.drawable.battery_full;
        }
    }

    private void endGame(String message) {
        // Show the message and final score
        Toast.makeText(this, message + "\nFinal Score: " + score, Toast.LENGTH_LONG).show();

        // Set Pikachu's image to dead
        creatureImage.setImageResource(R.drawable.pikachu_dead);

        // Stop all game loops
        gameHandler.removeCallbacks(gameLoop);
        hungerHandler.removeCallbacks(hungerDecrementRunnable);
        energyHandler.removeCallbacks(energyDecrementRunnable);
        sleepHandler.removeCallbacks(sleepChangeRunnable);
        happinessHandler.removeCallbacks(happinessDecrementRunnable);

        // Show the restart button and final score
        restartButton.setVisibility(Button.VISIBLE);
        scoreText.setText("Final Score: " + score);
    }

    private void restartGame() {
        hunger = 100;
        sleepiness = 100;
        happiness = 50;
        energy = 100;
        score = 0;
        isSleeping = false;

        restartButton.setVisibility(Button.INVISIBLE);
        updateUI();

        gameHandler.post(gameLoop);
        hungerHandler.post(hungerDecrementRunnable);
        energyHandler.post(energyDecrementRunnable);
        sleepHandler.post(sleepChangeRunnable);
        happinessHandler.post(happinessDecrementRunnable);
    }

    private void showFoodSelectionDialog() {
        String[] foodOptions = {"Apple", "Watermelon", "Cake", "Ice Cream"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Choose Food")
                .setItems(foodOptions, (dialog, which) -> {
                    if (hunger >= 90) {
                        Toast.makeText(this, "Pikachu is not hungry!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    switch (which) {
                        case 0:
                            feedPikachu(20, R.drawable.pikachu_eating_apple, "eats an apple!", "Apple", 2);
                            break;
                        case 1:
                            feedPikachu(30, R.drawable.pikachu_eating_watermelon, "eats watermelon!", "Watermelon", 1);
                            break;
                        case 2:
                            feedPikachu(40, R.drawable.pikachu_eating, "eats cake!", "Cake", 4);
                            break;
                        case 3:
                            feedPikachu(25, R.drawable.pikachu_eating_icecream, "eats ice cream!", "Ice Cream", 3);
                            break;
                    }
                }).show();
    }

    private void feedPikachu(int hungerAmount, int imageRes, String foodMessage, String foodType, int energyAmount) {
        // Если текущий тип еды совпадает с предыдущим
        if (lastFoodType != null && lastFoodType.equals(foodType)) {
            foodCounter++; // Увеличиваем счётчик
        } else {
            // Если тип еды меняется, сбрасываем счётчик и обновляем тип еды
            foodCounter = 0;
            lastFoodType = foodType;
        }

        // Проверка перекорма
        if (foodCounter >= 3) {
            Toast.makeText(this, "Pikachu is tired of eating " + foodType + "!", Toast.LENGTH_SHORT).show();
            energy = Math.max(0, energy - 10); // Уменьшаем энергию на 10
            energyImage.setImageResource(getEnergyStateImage(energy)); // Обновляем отображение энергии
            updateStateText(); // Обновляем текстовое отображение состояния
            if (energy == 0) {
                endGame("Pikachu is out of energy due to overfeeding.");
            }
            return;
        }

        // Проверка на сытость
        if (hunger >= 90) {
            Toast.makeText(this, "Pikachu is not hungry!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Если всё в порядке, обновляем показатели
        hunger = Math.min(100, hunger + hungerAmount); // Увеличиваем сытость
        happiness = Math.min(100, happiness + 5); // Увеличиваем счастье
        energy = Math.min(100, energy + energyAmount); // Увеличиваем энергию
        score += 5; // Увеличиваем счёт

        // Обновляем UI
        creatureImage.setImageResource(imageRes);
        Toast.makeText(this, "Pikachu " + foodMessage, Toast.LENGTH_SHORT).show();
        updateUI();
    }
}
