fun emulator() {
    var (a, b) = 100.0 to 100.0
    repeat(200) { g ->
        val game = g
        val (p1, p2) = Participant(a, 2, game) to Participant(b, 1, game)
        val delta = calcScoreChanges(p1, p2)
        a += delta.first
        b += delta.second
        println("$delta $a $b")
    }
}

fun main() {
    emulator()
}
