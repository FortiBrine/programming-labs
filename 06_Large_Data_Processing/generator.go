package main

import (
	"encoding/csv"
	"fmt"
	"math/rand"
	"os"
	"strconv"
	"time"
)

func main() {
	file, err := os.Create("players.csv")
	if err != nil {
		panic(err)
	}
	defer file.Close()

	writer := csv.NewWriter(file)
	defer writer.Flush()

	writer.Write([]string{
		"id",
		"username",
		"score",
		"level",
	})

	rand := rand.New(rand.NewSource(time.Now().UnixNano()))

	playerCount := 1000000

	for i := 1; i <= playerCount; i++ {
		username := fmt.Sprintf("Player%d", i)

		score := rand.Int63n(100000)

		level := rand.Intn(100) + 1

		record := []string{
			strconv.Itoa(i),
			username,
			strconv.FormatInt(score, 10),
			strconv.Itoa(level),
		}

		err := writer.Write(record)
		if err != nil {
			panic(err)
		}
	}

	fmt.Println("players.csv generated successfully")
}
