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

func getTopPlayers(filePath string, topCount int) ([]Player, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	reader := csv.NewReader(file)

	_, err = reader.Read()
	if err != nil {
		return nil, err
	}

	var topPlayers []Player

	for {
		row, err := reader.Read()

		if err == io.EOF {
			break
		}

		if err != nil {
			return nil, err
		}

		id, err := strconv.Atoi(row[0])
		if err != nil {
			return nil, err
		}

		score, err := strconv.ParseInt(row[2], 10, 64)
		if err != nil {
			return nil, err
		}

		levelInt, err := strconv.Atoi(row[3])
		if err != nil {
			return nil, err
		}

		player := Player{
			ID:       id,
			Username: row[1],
			Score:    score,
			Level:    byte(levelInt),
		}

		topPlayers = append(topPlayers, player)

		sort.Slice(topPlayers, func(i, j int) bool {
			if topPlayers[i].Level != topPlayers[j].Level {
				return topPlayers[i].Level > topPlayers[j].Level
			}

			return topPlayers[i].Score > topPlayers[j].Score
		})

		if len(topPlayers) > topCount {
			topPlayers = topPlayers[:topCount]
		}
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
