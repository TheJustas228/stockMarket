package com.example.stockmarketapp.models;

import com.example.stockmarketapp.NewsArticle;

import java.util.List;

public class NewsResponse {
    private List<NewsArticle> news;

    public List<NewsArticle> getNews() { return news; }
}