package me.simulador.dio.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.simulador.dio.R;

import me.simulador.dio.data.MatchesAPI;
import me.simulador.dio.databinding.ActivityMainBinding;
import me.simulador.dio.domain.Match;
import me.simulador.dio.ui.adapter.MatchesAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MatchesAPI matchesApi;
    private MatchesAdapter matchesAdapter = new MatchesAdapter(Collections.emptyList());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatingActionButton();

    }

    private void setupHttpClient() {
        Retrofit retrofit = new Retrofit.Builder().
                baseUrl("https://cristianoalves226.github.io/matches-simulator-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        matchesApi = retrofit.create(MatchesAPI.class);
    }

    private void setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener(view -> {
            view.animate().rotation(360).setDuration(508).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //TODO Implementar algoritmo de simulação de partida

                    Random random = new Random();

                    for (int i = 0; i < matchesAdapter.getItemCount(); i++) {
                        Match match = matchesAdapter.getMatches().get(i);
                        match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStarts() + 1));
                        match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStarts() + 1));
                        matchesAdapter.notifyItemChanged(i);
                    }
                }
            });
        });
    }

    private void setupMatchesRefresh() {
        binding.srMatches.setOnRefreshListener(this::findMatchesFromApi);
    }

    private void setupMatchesList() {
        binding.rvMarches.setHasFixedSize(true);
        binding.rvMarches.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMarches.setAdapter(matchesAdapter);
        findMatchesFromApi();
    }

    private void findMatchesFromApi() {
        binding.srMatches.setRefreshing(true);
        matchesApi.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(@NonNull Call<List<Match>> call,@NonNull Response<List<Match>> response) {
                if (response.isSuccessful()) {
                    List<Match> matches = response.body();
                    matchesAdapter = new MatchesAdapter(matches);
                    binding.rvMarches.setAdapter(matchesAdapter);
                } else {
                    showErrorMessage();
                }
                binding.srMatches.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                showErrorMessage();
                binding.srMatches.setRefreshing(false);
            }
        });
    }

    private void showErrorMessage() {
        Snackbar.make(binding.fabSimulate, R.string.error_api, Snackbar.LENGTH_LONG).show();
    }
}
