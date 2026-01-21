package com.gaizkafrost.mentxuapp.Parada6

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gaizkafrost.mentxuapp.BaseMenuActivity
import com.gaizkafrost.mentxuapp.Mapa.MapaActivity
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.repository.ParadasRepositoryMejorado
import com.gaizkafrost.mentxuapp.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Actividad para la Parada 6 del recorrido educativo.
 * Contiene un puzle de 5x5 y un test de preguntas.
 * Configurada como actividad de inicio (Launcher).
 */
class Parada6Activity : BaseMenuActivity() {

    // --- CONFIGURACIÓN DEL PUZLE ---
    private val PUZZLE_SIZE = 5 // 5x5
    private lateinit var puzzleRecyclerView: RecyclerView
    private lateinit var puzzleAdapter: PuzzleAdapter
    private var selectedPosition: Int = -1

    // --- CONFIGURACIÓN DEL TEST ---
    private lateinit var testContainer: LinearLayout
    private lateinit var quizRecyclerView: RecyclerView
    private lateinit var quizAdapter: QuizAdapter

    private lateinit var repository: ParadasRepositoryMejorado
    private lateinit var userPrefs: UserPreferences
    private var idParadaActual: Int = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parada6)

        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)
        idParadaActual = intent.getIntExtra("ID_PARADA", 6)

        setupPuzzle()
        setupQuiz()

        findViewById<Button>(R.id.btn_verify_test).setOnClickListener {
            checkQuizAnswers()
        }
    }

    // =========================================================================
    // LÓGICA DEL PUZLE
    // =========================================================================

    private fun setupPuzzle() {
        puzzleRecyclerView = findViewById(R.id.puzzle_recycler)
        
        // Crear las piezas (25 piezas para un 5x5)
        val pieces = mutableListOf<PuzzlePiece>()
        for (i in 0 until (PUZZLE_SIZE * PUZZLE_SIZE)) {
            val row = (i / PUZZLE_SIZE) + 1
            val col = (i % PUZZLE_SIZE) + 1
            
            // Busca el recurso R.drawable.fila_X_columna_Y (ahora con guiones bajos)
            val resourceName = "fila_${row}_columna_${col}"
            val resId = resources.getIdentifier(resourceName, "drawable", packageName)
            
            pieces.add(PuzzlePiece(i, resId))
        }

        // Aleatorizar las piezas
        pieces.shuffle()

        puzzleAdapter = PuzzleAdapter(pieces) { position ->
            handlePieceClick(position)
        }

        puzzleRecyclerView.layoutManager = GridLayoutManager(this, PUZZLE_SIZE)
        puzzleRecyclerView.adapter = puzzleAdapter
    }

    private fun handlePieceClick(position: Int) {
        if (selectedPosition == -1) {
            // Primer toque: seleccionar pieza
            selectedPosition = position
            puzzleAdapter.setSelected(position)
        } else {
            // Segundo toque: intercambiar
            if (selectedPosition != position) {
                puzzleAdapter.swapPieces(selectedPosition, position)
                checkPuzzleCompletion()
            }
            selectedPosition = -1
            puzzleAdapter.setSelected(-1)
        }
    }

    private fun checkPuzzleCompletion() {
        if (puzzleAdapter.isComplete()) {
            Toast.makeText(this, "Oso ondo! Puzlea osatu duzu.", Toast.LENGTH_LONG).show()
            findViewById<TextView>(R.id.puzzle_status).text = "Puzlea osatuta!"
            
            // Esperar 2 segundos antes de mostrar el test para que vean la imagen completa
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                unlockQuiz()
            }, 2000)
        }
    }

    private fun unlockQuiz() {
        testContainer = findViewById(R.id.test_container)
        testContainer.visibility = View.VISIBLE
        
        // Scroll automático suave hacia el test
        val scrollView = findViewById<androidx.core.widget.NestedScrollView>(R.id.parada6_scroll_view)
        testContainer.post {
            scrollView.smoothScrollTo(0, testContainer.top)
        }
    }

    // =========================================================================
    // LÓGICA DEL TEST
    // =========================================================================

    private fun setupQuiz() {
        quizRecyclerView = findViewById(R.id.quiz_recycler)
        val questions = getQuestions()
        quizAdapter = QuizAdapter(questions)
        quizRecyclerView.layoutManager = LinearLayoutManager(this)
        quizRecyclerView.adapter = quizAdapter
    }

    private fun checkQuizAnswers() {
        val results = quizAdapter.getResults()
        val correctCount = results.count { it }
        val total = results.size

        val resultArea = findViewById<TextView>(R.id.test_result_text)
        resultArea.visibility = View.VISIBLE
        resultArea.text = "${correctCount}/${total} erantzun dituzu ondo!"
        
        if (correctCount == total) {
            resultArea.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            
            // Marcamos la parada como completada en el Backend y Local
            val userId = userPrefs.userId
            val finalScore = calculateScore()
            val timeSpent = getElapsedTimeSeconds()
            
            lifecycleScope.launch {
                val isFreeMode = intent.getBooleanExtra("IS_FREE_MODE", false)
                if (!isFreeMode) {
                    repository.completarParada(userId, idParadaActual, finalScore, timeSpent)
                }
                
                // Mostrar puntuación y cerrar la actividad después de 3 segundos
                resultArea.postDelayed({
                    showScoreResult(finalScore)
                }, 3000)
            }
        } else {
            resultArea.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }
        
        quizAdapter.showFeedback()
    }

    /**
     * Define aquí las preguntas del test. 
     */
    private fun getQuestions(): List<Question> {
        return listOf(
            Question(1, "Nor da istorio honetako protagonista eta hitz egiten duena?", 
                listOf("Itsasargi bat.", "Mentxu kaioa.", "Sardina saltzaile bat."), 1),
            Question(2, "Noiz jaiotzen da Mentxu Santurtzin?", 
                listOf("Uztailaren 8an.", "Irailaren batean.", "Gabonetan."), 0),
            Question(3, "Zer adierazten du Mentxuren jaiotzak?", 
                listOf("Eskola bukatu dela.", "Jaiak hasi direla.", "San Joan suak piztuko direla."), 1),
            Question(4, "Zer gogorazten du ume eta txakurraren eskulturak?", 
                listOf("Santurtziko animaliak.", "Kirol txapelketa bat.", "Santurtziko iragan arrantzailea."), 2),
            Question(5, "Zer da Agurtza?", 
                listOf("Atunontzi-museo bat.", "Euskotren geltoki bat.", "Itsasargi bat."), 0),
            Question(6, "Zeren inguruan antolatzen zen Santurtziko bizitza aspaldian?", 
                listOf("Basogintzaren inguruan.", "Arrantzaren eta portuaren inguruan.", "Nekazaritzaren inguruan."), 1),
            Question(7, "Zergatik garrantzitsua da Santurtziko portua gaur egun?", 
                listOf("Jolasparke handi bat duelako.", "Herriaren ikurra delako eta itsasoari lotuta dagoelako.", "Itsasontziak daudelako."), 1),

            Question(8, "Zer gogorazten du “Monumento a los niños y niñas de la guerra” eskulturak?", 
                listOf("Gerra garaian ihes egin zuten haurrek.", "Oporretako bidaiak.", "Zientzia jaialdia."), 0)
        )
    }

    // =========================================================================
    // MODELOS DE DATOS
    // =========================================================================

    data class PuzzlePiece(val originalPosition: Int, val drawableResId: Int)

    data class Question(
        val id: Int,
        val text: String,
        val options: List<String>,
        val correctOptionIndex: Int
    )

    // =========================================================================
    // ADAPTADORES
    // =========================================================================

    inner class PuzzleAdapter(
        private val pieces: MutableList<PuzzlePiece>,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<PuzzleAdapter.ViewHolder>() {

        private var selectedPos = -1

        fun setSelected(position: Int) {
            val old = selectedPos
            selectedPos = position
            if (old != -1) notifyItemChanged(old)
            if (selectedPos != -1) notifyItemChanged(selectedPos)
        }

        fun swapPieces(from: Int, to: Int) {
            val temp = pieces[from]
            pieces[from] = pieces[to]
            pieces[to] = temp
            notifyItemChanged(from)
            notifyItemChanged(to)
        }

        fun isComplete(): Boolean {
            for (i in pieces.indices) {
                if (pieces[i].originalPosition != i) return false
            }
            return true
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_puzzle_piece, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val piece = pieces[position]
            if (piece.drawableResId != 0) {
                holder.image.setImageResource(piece.drawableResId)
            } else {
                holder.image.setImageResource(android.R.drawable.ic_menu_report_image)
            }
            
            holder.overlay.visibility = if (position == selectedPos) View.VISIBLE else View.GONE
            holder.itemView.setOnClickListener { onClick(position) }
        }

        override fun getItemCount() = pieces.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image: ImageView = view.findViewById(R.id.puzzle_image)
            val overlay: View = view.findViewById(R.id.selection_overlay)
        }
    }

    inner class QuizAdapter(private val questions: List<Question>) : RecyclerView.Adapter<QuizAdapter.ViewHolder>() {
        
        private val userAnswers = IntArray(questions.size) { -1 }
        private var showFeedback = false

        fun getResults(): List<Boolean> {
            return questions.mapIndexed { index, question -> 
                userAnswers[index] == question.correctOptionIndex
            }
        }

        fun showFeedback() {
            showFeedback = true
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quiz_question, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val q = questions[position]
            holder.questionText.text = "${position + 1}. ${q.text}"
            holder.rbA.text = q.options[0]
            holder.rbB.text = q.options[1]
            holder.rbC.text = q.options[2]

            holder.radioGroup.setOnCheckedChangeListener(null)

            when (userAnswers[position]) {
                0 -> holder.rbA.isChecked = true
                1 -> holder.rbB.isChecked = true
                2 -> holder.rbC.isChecked = true
                else -> holder.radioGroup.clearCheck()
            }

            holder.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                val index = when (checkedId) {
                    R.id.option_a -> 0
                    R.id.option_b -> 1
                    R.id.option_c -> 2
                    else -> -1
                }
                userAnswers[position] = index
            }

            if (showFeedback) {
                holder.feedback.visibility = View.VISIBLE
                val isCorrect = userAnswers[position] == q.correctOptionIndex
                if (isCorrect) {
                    holder.feedback.text = "Zuzena!"
                    holder.feedback.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                } else {
                    val correctLetter = when(q.correctOptionIndex) {
                        0 -> "a"
                        1 -> "b"
                        else -> "c"
                    }
                    holder.feedback.text = "Okerra. Erantzun zuzena: $correctLetter)"
                    holder.feedback.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                }
            } else {
                holder.feedback.visibility = View.GONE
            }
        }

        override fun getItemCount() = questions.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val questionText: TextView = view.findViewById(R.id.question_text)
            val radioGroup: RadioGroup = view.findViewById(R.id.options_group)
            val rbA: RadioButton = view.findViewById(R.id.option_a)
            val rbB: RadioButton = view.findViewById(R.id.option_b)
            val rbC: RadioButton = view.findViewById(R.id.option_c)
            val feedback: TextView = view.findViewById(R.id.question_feedback)
        }
    }
}
