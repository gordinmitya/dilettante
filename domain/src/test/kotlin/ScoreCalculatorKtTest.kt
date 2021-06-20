import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val ANY_RATING = 100.0
private const val WIN_SCORE = 3
private const val LOS_SCORE = 1
private const val MANY_GAMES = 10

private fun make(r: Double = ANY_RATING, s: Int = WIN_SCORE, g: Int = MANY_GAMES) = Participant(r, s, g)

private inline fun <A> swappableTest(a: A, b: A, block: (A, A) -> Unit) {
    block(a, b)
    block(b, a)
}

internal class ScoreCalculatorKtTest {
    @Test
    fun tieDoNotAffect() {
        swappableTest(make(s = 1), make(s = 1)) { first, second ->
            val result = calcScoreChanges(first, second)
            assertEquals(0.0, result.first)
            assertEquals(0.0, result.second)
        }
    }

    @Test
    fun tooCoolWinnerDoNotAffect() {
        swappableTest(make(300.0, WIN_SCORE), make(10.0, LOS_SCORE)) { first, second ->
            val result = calcScoreChanges(first, second)
            assertEquals(0.0, result.first)
            assertEquals(0.0, result.second)
        }
    }

    @Test
    fun tooCoolLoserAffect() {
        swappableTest(make(300.0, LOS_SCORE), make(10.0, WIN_SCORE)) { first, second ->
            val result = calcScoreChanges(first, second)
            assertNotEquals(0.0, result.first)
            assertNotEquals(0.0, result.second)
        }
    }

    private fun assertK(expected: Double, p: Participant, avg: Double) {
        val resultW = calcWinnerK(p, avg)
        val resultL = calcLoserK(p, avg)
        assertEquals(expected, resultW)
        assertEquals(resultW, resultL)
    }

    @Test
    fun chooseTournamentCoefficient() {
        assertK(0.2, make(), (250.0 + 1.0) / 2.0)
        assertK(0.25, make(), (350.0 + 250.0) / 2.0)
        assertK(0.3, make(), (450.0 + 350.0) / 2.0)
        assertK(0.3, make(), (450.0 + 350.0) / 2.0)
        assertK(0.35, make(), (550.0 + 450.0) / 2.0)
        assertK(0.4, make(), (1000.0 + 550.0) / 2.0)

        assertK(0.25, make(), 326.47)
    }

    @Test
    fun chooseTournamentCoefficientNewcomer() {
        assertEquals(1.0, calcWinnerK(make(s = WIN_SCORE, g = 1), ANY_RATING))
        assertEquals(0.5, calcLoserK(make(s = LOS_SCORE, g = 1), ANY_RATING))
    }

    private fun assertD(expected: Double, winner: Participant, loser: Participant) {
        val dPair = calcD(winner, loser)
        assertEquals(expected, dPair.second)
        assertEquals(dPair.first, dPair.second)
    }

    @Test
    fun chooseScoreCoefficient() {
        assertD(0.8, make(s = 3), make(s = 2))
        assertD(1.0, make(s = 3), make(s = 1))
        assertD(1.2, make(s = 3), make(s = 0))
    }

    @Test
    fun chooseScoreCoefficientNewComer() {
        val dPair = calcD(make(s = WIN_SCORE, g = 1), make(s = LOS_SCORE, g = 1))
        assertEquals(1.0, dPair.first)
        assertEquals(1.0, dPair.second)
    }

    // https://rttf.ru/tournaments/8509
    @Test
    fun testScoreChanges() = swappableTest(make(327.0, 4), make(265.0, 2)) { first, second ->
        val deltas = calcScoreChanges(first, second, 326.47)
        assertEquals(deltas.first, -deltas.second)
        assertEquals(0.95, abs(deltas.first))
    }
}
