package com.example.unscramble

import androidx.lifecycle.ViewModel
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    //endregion

    //region  1.UiState data class
    // 流向 view 的数据类,这里定义的数据将会作为画面的 state 值,每次修改都会导致画面的重组
    data class GameUiState(
        val currentScrambledWord: String = ""
    )
    //endregion
}