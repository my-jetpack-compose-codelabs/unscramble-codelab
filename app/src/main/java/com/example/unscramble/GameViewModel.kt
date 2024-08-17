package com.example.unscramble

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel: ViewModel() {
    /*
    mutableStateFlow的特性
    多播特性: MutableStateFlow 可以有多个观察者（收集器），所有观察者都会在状态变化时收到通知。
    流 (Flow) 的特性: 它是一个热流，始终会发射最新的值。当新的观察者订阅时，会立即接收当前的最新值。
    线程安全: 由于 MutableStateFlow 是基于 Coroutines 的，适合在多线程环境中使用。
    使用场景: MutableStateFlow 适合用于在 ViewModel 或其他非 Compose 特定的层中管理和共享状态，特别是需要跨多个组件共享状态时。
    总结: viewmodel 对比 view,可能有更多的观察者,也需要处理一些异步的方法,所以对比mutablestateof,viewmodel 更适合使用MutableStateFlow
    */

    //region 2.property
    // 创建一个MutableStateFlow类型的后备字段,供 viewmodel 内部的事件方法处理
    private val _uiState = MutableStateFlow(GameUiState())
    // 创建一个公开的,不可修改的StateFlowe类型的属性,供画面参照
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // 当前的,打乱前的 word 属性
    // Tips: 使用 lateinit, 在 kotlin 中,属性是必须提供默认值或者在初始化方法提供初始化的处理,使用 lateinit 可以手动指定初始化的时机,但是如果在调用前没有初始化,则会报错
    private lateinit var currentWord: String

    // 创建一个 可变set 来存储已经出题过的 words
    // Tips: set 是乱序且没有重复元素的类似数组的类型,当不需要重复元素和对数据有顺序需求的时候可以用 set
    private var usedWords: MutableSet<String> = mutableSetOf()

    // 创建一个state 的字符串,让textField 可以参照显示
    // 这里也没使用了 remember,是因为viewmodel 和@Composable 和 activity,fragment 不同,在重组或者改变屏幕朝向的时候,不会销毁重建,所以不需要使用 remember 就可以维持
    // 另外这里,没有将 userGuess 这个属性定义在 data class 中,因为userGuess是一个频繁修正且只和view 组件的关联比较单纯,也无关异步等 flow 才可以实现的需求,所以在简洁性,更新时开销和效率的考虑,直接使用mutableStateOf由 viewmodel 直接持有是可以的,这不是一个非黑即白的规则,要根据需求灵活的考虑
    var userGuess by mutableStateOf("")
        // 这里把userGuess的 setter 方法给私有了,所以实现了在外部可以参照但是没办法直接修改,而是必须要依赖于updateUserGuess方法去修改,保证 udf 数据单向流的规则
        private set

    //endregion

    //region init
    init {
        // 初始化游戏
        resetGame()
    }
    //endregion

    //region 3.helper method
    // 从 words库 里面随机一个 word,打乱后返回
    private fun pickRandomWordAndShuffle(): String {
        // 从WordsData.kt的allWords中随机找到一个 word
        currentWord = allWords.random()

        if (usedWords.contains(currentWord)) {
            // 当这个 word 已经用过了就递归处理的再找一个
            return pickRandomWordAndShuffle()
        } else {
            // 如果这个 word 还没用过就添加到已用 set(usedWords)中,然后返回打乱后的 word
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    // 打乱 word 的辅助方法
    private fun shuffleCurrentWord(word: String): String {
        // 一个临时的CharArray类型的属性
        val tempWord: CharArray = word.toCharArray()
        // 打乱这个CharArray
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            // 当这个CharArray和打乱前相同,就循环打乱直到和原本的字符串不同
            tempWord.shuffle()
        }
        // 返回打乱后的字符串
        return String(tempWord)
    }

    // 重置/初始化游戏的方法
    fun resetGame() {
        // 清除已使用词库
        usedWords.clear()
        // 创建一个全新的 state 实例,供画面参照
        _uiState.value = GameUiState(
            currentScrambledWord = pickRandomWordAndShuffle()
        )
    }

    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            // 当问题的计数达到了每轮游戏的上限
            _uiState.update {
                // 算分,修改状态为 game over
                it.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else {
            // 当问题计数还没到上限,
            _uiState.update {
                it.copy(
                    // 重新选一个新的问题
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    // 初始化对错的状态
                    isGuessedWordWrong = false,
                    // 更新分数
                    score = updatedScore,
                    // 更新计数, 这里的.inc()就是++调用的方法,注意+=1 是调用.plus(1), 另外.inc()只能计算+1
                    currentWordCount = it.currentWordCount.inc()
                )
            }
        }
    }

    //endregion

    //region  4.event method
    fun updateUserGuess(guessedWord: String) {
        // 会在用户输入的文字有变时机会执行这个方法,修改userGuess的值
        userGuess = guessedWord
    }

    // 在我们的 app 式样中,点击键盘的 done 和 submit 按钮都会检查输入文字是否正确
    fun checkUserGuess() {
        // 检查用户答案和问题的方法,如果一致就下一题,如果不一致就报错
        // 这里的ignoreCase = true是是否无视大小写
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // 计算积分,SCORE_INCREASE是一个常量, 这里使用了.plus 语法, 其实+的本质就是调用.plus 语法,所以可以自由选择
            // 另外这里没有使用+=或者++,请参考错误我在回答错误处理处对 val 的解释
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            // 添加一个更新状态的方法去处理, 涉及逻辑有算分,移动下一题等
            updateGameState(updatedScore)
        } else {
            // 如果用户的答案不对,就把 uistate 的 isGuessedWordWrong 值改为 true
            _uiState.update { currentState ->
                // 这里是用的MutableStateFlow的 update 方法更新值, 关于下列的 copy 的用法,比较简单不做赘述
                // Tips1: 注意 private val _uiState, 这里使用的 val, 说的是 _uiState 这个实例是不能变的,也就是没办法赋一个新的_uiState,但是_uiState本身的属性是可变的
                // Tips2: 进一步,尝试 _uiState.value.isGuessedWordWrong = false, 会发现报错,是因为 data class 的声明中是 val isGuessedWordWrong: Boolean = false
                // Tips3: 再进一步, 这里我们使用了 update 来对 _uiState的值进行更新,除了可以保证数据的原子性(*transaction 事务)和简化代码, 还有一方面这也是贯彻 UDF 设计理念
                // Tips4: 使用 _uiState.value.isGuessedWordWrong = false这样方式,还可能会导致state 无法正确引发画面重组,(未测试, 根据 AI 的回答得知s)
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // 无论用户输入的答案的对与错,check 后都会清空用户的输入文字
        updateUserGuess("")
    }

    fun skipWord() {
        // 处理跳过问题的事件,分数保持不变
        updateGameState(_uiState.value.score)
    }
    //endregion


    //region  1.UiState data class
    // 流向 view 的数据类,这里定义的数据将会作为画面的 state 值,每次修改都会导致画面的重组
    data class GameUiState(
        val currentScrambledWord: String = "",
        // 添加一个布尔值,表示用于输入的答案的对与否
        val isGuessedWordWrong: Boolean = false,
        // 分数
        val score: Int = 0,
        // 现在回答问题的计数
        val currentWordCount: Int = 1,
        // 判断游戏是否结束的 flag
        val isGameOver: Boolean = false
    )
    //endregion
}