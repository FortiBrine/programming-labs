package main

import (
	"encoding/csv"
	"fmt"
	"io"
	"log"
	"os"
	"sort"
	"strconv"
)

type Player struct {
	ID       int
	Username string
	Score    int64
	Level    byte
}

func streamPlayers(filePath string) (<-chan Player, <-chan error) {
	players := make(chan Player)
	errors := make(chan error, 1)

	go func() {
		defer close(players)
		defer close(errors)

		file, err := os.Open(filePath)
		if err != nil {
			errors <- err
			return
		}
		defer file.Close()

		reader := csv.NewReader(file)

		_, err = reader.Read()
		if err != nil {
			errors <- err
			return
		}

		for {
			row, err := reader.Read()

			if err == io.EOF {
				break
			}

			if err != nil {
				errors <- err
				return
			}

			id, err := strconv.Atoi(row[0])
			if err != nil {
				errors <- err
				return
			}

			score, err := strconv.ParseInt(row[2], 10, 64)
			if err != nil {
				errors <- err
				return
			}

			level, err := strconv.Atoi(row[3])
			if err != nil {
				errors <- err
				return
			}

			players <- Player{
				ID:       id,
				Username: row[1],
				Score:    score,
				Level:    byte(level),
			}
		}
	}()

	return players, errors
}

func isBetter(a, b Player) bool {
	if a.Level != b.Level {
		return a.Level > b.Level
	}

	return a.Score > b.Score
}

func getTopPlayers(filePath string, topCount int) ([]Player, error) {
	playerStream, errors := streamPlayers(filePath)

	var topPlayers []Player

	for player := range playerStream {
		topPlayers = append(topPlayers, player)

		sort.Slice(topPlayers, func(i, j int) bool {
			return isBetter(topPlayers[i], topPlayers[j])
		})

		if len(topPlayers) > topCount {
			topPlayers = topPlayers[:topCount]
		}
	}

	if err := <-errors; err != nil {
		return nil, err
	}

	return topPlayers, nil
}

func main() {
	topPlayers, err := getTopPlayers("players.csv", 10)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println("Top players:")

	for i, player := range topPlayers {
		fmt.Printf(
			"%d. ID=%d | Username=%s | Level=%d | Score=%d\n",
			i+1,
			player.ID,
			player.Username,
			player.Level,
			player.Score,
		)
	}
}
