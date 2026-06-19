package main

import (
	"encoding/json"
	"flag"
	"log"
	"net/http"
	"strconv"

	"github.com/Fribb/anime-lists-generator/go/anime"
)

var lookup *anime.Lookup

func handleLookup(w http.ResponseWriter, r *http.Request) {
	malStr := r.URL.Query().Get("mal-id")
	if malStr == "" {
		http.Error(w, `{"error":"missing mal-id query parameter"}`, http.StatusBadRequest)
		return
	}

	malID, err := strconv.Atoi(malStr)
	if err != nil {
		http.Error(w, `{"error":"mal-id must be an integer"}`, http.StatusBadRequest)
		return
	}

	result := lookup.LookupMAL(malID)
	if result == nil {
		http.Error(w, `{"error":"no entry found for the given mal-id"}`, http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}

func main() {
	addr := flag.String("addr", ":8080", "server listen address")
	dataPath := flag.String("data", "data/anime-list-full.json", "path to anime-list-full.json")
	flag.Parse()

	var err error
	lookup, err = anime.NewLookup(*dataPath)
	if err != nil {
		log.Fatalf("failed to load data: %v", err)
	}
	log.Printf("loaded %d anime entries (%d with MAL IDs)", lookup.Len(), lookup.MALCount())

	mux := http.NewServeMux()
	mux.HandleFunc("/lookup", handleLookup)

	log.Printf("starting server on %s", *addr)
	if err := http.ListenAndServe(*addr, mux); err != nil {
		log.Fatalf("server error: %v", err)
	}
}
