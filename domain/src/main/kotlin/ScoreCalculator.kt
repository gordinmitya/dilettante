import kotlin.math.*

const val BIG_DIFFERENCE = 100.0
const val NEWCOMER_GAMES_COUNT = 5

class Participant(
    val rating: Double, // initial rating before the game
    val score: Int, // result of the game eg 4:3
    val gamesCount: Int // number of played games without this one
)

private fun Participant.isNewComer() =
    gamesCount <= NEWCOMER_GAMES_COUNT

typealias Changes = Pair<Double, Double>

// formulas taken from https://rttf.ru/content/2
// why not the official ФНТР rating https://rttf.ru/content/91
fun calcScoreChanges(first: Participant, second: Participant): Changes {
    val avg = (first.rating + second.rating) / 2.0
    return calcScoreChanges(first, second, avg)
}

fun calcScoreChanges(first: Participant, second: Participant, avgTournamentRating: Double): Changes {
    return if (first.score >= second.score) {
        calcScoreChangesInternal(first, second, avgTournamentRating)
    } else {
        calcScoreChangesInternal(second, first, avgTournamentRating).swap()
    }
}

private fun calcScoreChangesInternal(winner: Participant, loser: Participant, avgTournamentRating: Double): Changes {
    // tie
    if (winner.score == loser.score) {
        return 0.0 to 0.0
    }
    require(winner.score > loser.score)

    // too big difference
    if (winner.rating - loser.rating > BIG_DIFFERENCE) {
        return 0.0 to 0.0
    }

    val deltaPart = (100.0 - (winner.rating - loser.rating)) / 10.0
    val kWinner = calcWinnerK(winner, avgTournamentRating)
    val kLoser = calcLoserK(loser, avgTournamentRating)
    val dPair = calcD(winner, loser)

    val winnerDelta = deltaPart * kWinner * dPair.first
    // rating can't be less than 1
    val loserDelta = (deltaPart * kLoser * dPair.second).coerceAtMost(loser.rating - 1.0)
    require(loser.rating - loserDelta >= 1.0)

    return winnerDelta to -loserDelta
}

private fun kFromAvg(avg: Double): Double = when {
    avg < 250 -> 0.2
    avg < 350 -> 0.25
    avg < 450 -> 0.3
    avg < 550 -> 0.35
    else -> 0.4
}

internal fun calcWinnerK(winner: Participant, avg: Double): Double {
    return if (winner.isNewComer()) 1.0 else kFromAvg(avg)
}

internal fun calcLoserK(loser: Participant, avg: Double): Double {
    return if (loser.isNewComer()) 0.5 else kFromAvg(avg)
}

private fun dFromDiff(diff: Int): Double = when (abs(diff)) {
    0 -> throw IllegalArgumentException("tie should be handled before")
    1 -> 0.8
    2 -> 1.0
    else -> 1.2
}

internal fun calcD(winner: Participant, loser: Participant): Pair<Double, Double> {
    require(winner.score > loser.score)
    val dCommon = dFromDiff(winner.score - loser.score)
    val winnerD = if (winner.isNewComer()) 1.0 else dCommon
    val loserD = if (loser.isNewComer()) 1.0 else dCommon
    return winnerD to loserD
}
