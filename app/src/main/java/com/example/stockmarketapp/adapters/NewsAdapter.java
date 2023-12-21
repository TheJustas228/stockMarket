package com.example.stockmarketapp.adapters;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockmarketapp.NewsArticle;
import com.example.stockmarketapp.R;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private final List<NewsArticle> newsArticles;

    public NewsAdapter(List<NewsArticle> newsArticles) {
        this.newsArticles = newsArticles;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_article_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        NewsArticle article = newsArticles.get(position);

        String titleWithSymbol = article.getSymbol() != null ? "(" + article.getSymbol() + ") " + article.getTitle() : article.getTitle();
        holder.titleTextView.setText(titleWithSymbol);

        Log.d("NewsAdapter", "Article symbol: " + article.getSymbol() + ", title: " + article.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (article.getUrl() != null && !article.getUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getUrl()));
                v.getContext().startActivity(intent);
            } else {
                Toast.makeText(v.getContext(), "This article does not have a link.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsArticles.size();
    }

    public void addNewsArticle(NewsArticle newsArticle) {
        this.newsArticles.add(newsArticle);
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        public NewsViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
        }
    }
}
