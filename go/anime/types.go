package anime

type Item struct {
	Type          string          `json:"type"`
	AnidbID       *int            `json:"anidb_id,omitempty"`
	AnilistID     *int            `json:"anilist_id,omitempty"`
	MyanimelistID *int            `json:"mal_id,omitempty"`
	TheMovieDbID  *TheMovieDBItem `json:"themoviedb_id,omitempty"`
	TvdbID        *int            `json:"tvdb_id,omitempty"`
	Season        *Season         `json:"season,omitempty"`
	EpisodeOffset *EpisodeOffset  `json:"episode_offset,omitempty"`
}

type TheMovieDBItem struct {
	Movie []int `json:"movie,omitempty"`
	Tv    *int  `json:"tv,omitempty"`
}

type Season struct {
	Thetvdb    *int `json:"tvdb,omitempty"`
	TheMovieDb *int `json:"tmdb,omitempty"`
}

type EpisodeOffset struct {
	Thetvdb    *int `json:"tvdb,omitempty"`
	TheMovieDb *int `json:"tmdb,omitempty"`
}
