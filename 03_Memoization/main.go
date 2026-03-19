package main

import (
	"fmt"
	"hash/fnv"
	"time"
)

type CacheItem[R any] struct {
	Value       R
	AccessCount int
	LastUsed    time.Time
	ExpiresAt   time.Time
}

type Memoizer[A comparable, R any] struct {
	Capacity     int
	Expiration   time.Duration
	Policy       string
	CustomPolicy func(storage map[A]CacheItem[R]) A
}

func (m *Memoizer[A, R]) Memoize(f func(A) R) func(A) R {
	storage := make(map[A]CacheItem[R])

	cleanupExpired := func() {
		now := time.Now()

		if m.Expiration > 0 {
			for key, item := range storage {
				if now.After(item.ExpiresAt) {
					delete(storage, key)
				}
			}
		}
	}

	pruneOne := func() bool {
		if len(storage) == 0 {
			return false
		}

		var keyToRemove A

		switch m.Policy {
		case "LRU":
			var oldestTime time.Time
			first := true

			for key, item := range storage {
				if first || item.LastUsed.Before(oldestTime) {
					oldestTime = item.LastUsed
					keyToRemove = key
					first = false
				}
			}
		case "LFU":
			var minCount int
			first := true

			for key, item := range storage {
				if first || item.AccessCount < minCount {
					minCount = item.AccessCount
					keyToRemove = key
					first = false
				}
			}
		default:
			if m.CustomPolicy == nil {
				return false
			}

			keyToRemove = m.CustomPolicy(storage)
		}

		delete(storage, keyToRemove)
		return true
	}

	return func(key A) R {
		now := time.Now()
		cleanupExpired()

		if item, ok := storage[key]; ok {
			item.AccessCount++
			item.LastUsed = now
			storage[key] = item

			return item.Value
		}

		value := f(key)

		newEntry := CacheItem[R]{
			Value:       value,
			LastUsed:    now,
			AccessCount: 1,
		}

		if m.Expiration > 0 {
			newEntry.ExpiresAt = now.Add(m.Expiration)
		}

		if m.Capacity > 0 && len(storage) >= m.Capacity {
			if !pruneOne() {
				return value
			}
		}

		storage[key] = newEntry

		return value
	}
}

func main() {
	getUserBalance := func(email string) float64 {
		fmt.Println("getting user balance... please wait..")

		h := fnv.New32a()
		h.Write([]byte(email))
		return 100 + float64(h.Sum32()%90000)/100
	}

	lruMemoizer := Memoizer[string, float64]{
		Capacity: 2,
		Policy:   "LRU",
	}

	lruMemoizedFunc := lruMemoizer.Memoize(getUserBalance)

	fmt.Println(lruMemoizedFunc("user1@fortibrine.me"))
	fmt.Println(lruMemoizedFunc("user1@fortibrine.me"))
	fmt.Println(lruMemoizedFunc("user2@fortibrine.me"))
	fmt.Println(lruMemoizedFunc("user3@fortibrine.me"))
	fmt.Println(lruMemoizedFunc("user1@fortibrine.me"))

	fmt.Println()

	lfuMemoizer := Memoizer[string, float64]{
		Capacity:   2,
		Policy:     "LFU",
		Expiration: 5 * time.Second,
	}

	lfuMemoizedFunc := lfuMemoizer.Memoize(getUserBalance)

	fmt.Println(lfuMemoizedFunc("user1@fortibrine.me")) // 1
	fmt.Println(lfuMemoizedFunc("user1@fortibrine.me")) // 2
	fmt.Println(lfuMemoizedFunc("user2@fortibrine.me")) // 1
	fmt.Println(lfuMemoizedFunc("user3@fortibrine.me")) // 1 (remove user2)
	fmt.Println(lfuMemoizedFunc("user1@fortibrine.me")) // 3
	fmt.Println(lfuMemoizedFunc("user2@fortibrine.me")) // 1

	time.Sleep(6 * time.Second)
	fmt.Println(lfuMemoizedFunc("user1@fortibrine.me")) // 1

}
