package main

import (
	"fmt"
	"hash/fnv"
	"time"
)

type CacheItem[R any] struct {
	value     R
	expiresAt time.Time
}

type Memoizer2[A comparable, R any] struct {
	MaxSize      int
	LiveDuration time.Duration
	Policy       string
	CustomPolicy func(cache map[A]CacheItem[R]) A
}

func (m *Memoizer2[A, R]) Memoize(f func(A) R) func(A) R {
	cache := make(map[A]CacheItem[R])

	return func(arg A) R {
		now := time.Now()

		if m.LiveDuration != 0 {
			for key, item := range cache {
				if now.After(item.expiresAt) {
					delete(cache, key)
				}
			}
		}

		if item, ok := cache[arg]; ok {
			return item.value
		}

		result := f(arg)

		entry := CacheItem[R]{
			value: result,
		}

		if m.LiveDuration != 0 {
			entry.expiresAt = now.Add(m.LiveDuration)
		}

		cache[arg] = entry

		return result
	}
}

func main() {
	getUserBalance := func(email string) float64 {
		fmt.Println("getting user balance... please wait..")

		h := fnv.New32a()
		h.Write([]byte(email))
		return 100 + float64(h.Sum32()%90000)/100
	}

	memoizer := Memoizer2[string, float64]{
		MaxSize:      1,
		Policy:       "LRU",
		LiveDuration: time.Duration(5) * time.Second,
	}

	memoizedF := memoizer.Memoize(getUserBalance)

	fmt.Println(memoizedF("user1@fortibrine.me"))
	fmt.Println(memoizedF("user1@fortibrine.me"))
	fmt.Println(memoizedF("user2@fortibrine.me"))

}
