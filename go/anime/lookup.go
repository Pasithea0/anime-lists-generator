package anime

import (
	"encoding/json"
	"fmt"
	"os"
)

type Lookup struct {
	items []Item
	byMAL map[int]*Item
}

func (l *Lookup) Len() int      { return len(l.items) }
func (l *Lookup) MALCount() int { return len(l.byMAL) }

func NewLookup(dataPath string) (*Lookup, error) {
	raw, err := os.ReadFile(dataPath)
	if err != nil {
		return nil, fmt.Errorf("read data file: %w", err)
	}

	var items []Item
	if err := json.Unmarshal(raw, &items); err != nil {
		return nil, fmt.Errorf("parse data file: %w", err)
	}

	byMAL := make(map[int]*Item, len(items))
	for i := range items {
		item := &items[i]
		if item.MyanimelistID != nil {
			byMAL[*item.MyanimelistID] = item
		}
	}

	return &Lookup{items: items, byMAL: byMAL}, nil
}

func (l *Lookup) ByMAL(malID int) *Item {
	return l.byMAL[malID]
}

type LookupResult struct {
	MALID         int              `json:"mal_id"`
	TMDBID        *TheMovieDBItem  `json:"tmdb_id,omitempty"`
	TVDBID        *int             `json:"tvdb_id,omitempty"`
	Season        *Season          `json:"season,omitempty"`
	EpisodeOffset *EpisodeOffset   `json:"episode_offset,omitempty"`
}

func (l *Lookup) LookupMAL(malID int) *LookupResult {
	item := l.ByMAL(malID)
	if item == nil {
		return nil
	}
	return &LookupResult{
		MALID:         malID,
		TMDBID:        item.TheMovieDbID,
		TVDBID:        item.TvdbID,
		Season:        item.Season,
		EpisodeOffset: item.EpisodeOffset,
	}
}
