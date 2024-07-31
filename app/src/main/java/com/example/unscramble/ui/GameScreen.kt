/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.unscramble.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 注意这里需要导入的是 compose 的 viewmodel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unscramble.GameViewModel
import com.example.unscramble.R
import com.example.unscramble.ui.theme.UnscrambleTheme

/*
关于 UDF (Unidirectional Data Flow) 单向数据流
单向数据流动: 数据的变化始终沿着一个方向流动，通常是从数据源到 UI，然后从 UI 触发动作，导致数据源的更新。
可预测性: 因为数据流动是单向的，应用的状态变化更加可预测，调试也更容易。
简化状态管理: 由于数据流动的路径是固定的，管理和维护应用的状态变得更简单。

总结:如同我们实装的 viewmodel 一般, viewmodel 的公开的 state 属性只允许 view 参照不允许修改,这样就实现了数据从 viewmodel(也可以叫做状态容器)单方面的流向 view,而 view 向上传递的则是事件(如点击事件,滑动事件),viewmodel 会根据事件执行相应的方法(method) 处理 state 数据,state 数据的更新又会引起Composable组件的重组,这样就达成了完整的单向数据流
Tips: 这里的实现可以参照 google 对 uilayer 中的描述,可以平行的了解 layer 的框架和概念
https://developer.android.com/topic/architecture/ui-layer?hl=zh-cn
 */

// Tips: 在Composable的组件代码内,代码结构可能会比较复杂,所以可以活用 android studio 的 structure 工具,帮助你找到位置
@Composable
fun GameScreen(
    // 将我们的 GameViewModel 注册为 compose 组件的 viewmodel
    gameViewModel: GameViewModel = viewModel()
) {
    // 将gameViewModel里面的uiState值,从 flow 类型转换为 compose 组件使用的 state 类型,且collectAsState是带有状态的订阅和记忆的逻辑,所以不需要使用 remember 也可以在翻转屏幕时维持数据
    val gameUiState by gameViewModel.uiState.collectAsState()

    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.app_name),
            style = typography.titleLarge,
        )
        GameLayout(
            // 这里onUserGuessChanged接受了一个Lambda式,类似于接受了一个代码块,这是一个高阶函数的应用
            // { gameViewModel.updateUserGuess(it) }是 { it -> gameViewModel.updateUserGuess(it) }的简写, 因为只有一个参数,当有两个参数或跟多则需要自己手动指定了
            // Tips: 高阶函数是说,一个函数 A 的接受的参数或者返回值是一个函数,这个A 就是高阶函数,所以GameLayout在这里是高阶函数
            onUserGuessChanged = { gameViewModel.updateUserGuess(it) },
            onKeyboardDone = { },
            // 这里的gameViewModel.userGuess,当 userGuess 字段修正会使GameScreen和GameLayout都重组,但是 compose 会只能略过 GameScreen 中不依赖于 userGuess 的 ui 部分提高效率,这里的操作不需要手动管理的,另外测试 composable 组件重组的方式可以直接在 fun 里面写 print 打印的代码,就会在重组的时候打印了
            userGuess = gameViewModel.userGuess,
            currentScrambledWord = gameUiState.currentScrambledWord,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(mediumPadding)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(mediumPadding),
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { }
            ) {
                Text(
                    text = stringResource(R.string.submit),
                    fontSize = 16.sp
                )
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.skip),
                    fontSize = 16.sp
                )
            }
        }

        GameStatus(score = 0, modifier = Modifier.padding(20.dp))
    }
}

@Composable
fun GameStatus(score: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.score, score),
            style = typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
// 出题区和答案输入区的组件部分,所以需要从 viewmodel 获取一些数据才能实现后续的功能
/*
数据流入:
1. 当前的问题 text -> gameUiState.currentScrambledWord
2. 当前输入的 text -> gameViewModel.userGuess

事件上报:
1. 输入答案文字改变 -> { gameViewModel.updateUserGuess(it) }
2. 键盘 done 键按下 -> TODO
 */
fun GameLayout(
    onUserGuessChanged: (String) -> Unit,
    onKeyboardDone: () -> Unit,
    userGuess: String,
    currentScrambledWord: String,
    modifier: Modifier = Modifier
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(mediumPadding)
        ) {
            Text(
                modifier = Modifier
                    .clip(shapes.medium)
                    .background(colorScheme.surfaceTint)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .align(alignment = Alignment.End),
                text = stringResource(R.string.word_count, 0),
                style = typography.titleMedium,
                color = colorScheme.onPrimary
            )
            // 这个 text 组件就是问题的text, 可以通过点击 preview 上的组件直接定位到代码
            Text(
                text = currentScrambledWord,
                style = typography.displayMedium
            )
            Text(
                text = stringResource(R.string.instructions),
                textAlign = TextAlign.Center,
                style = typography.titleMedium
            )
            OutlinedTextField(
                // 把 textField 显示的文字设置为gameViewModel.userGuess
                value = userGuess,
                singleLine = true,
                shape = shapes.large,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                // 当textField输入的内容变动的时候出发的行为
                // Tips: onValueChange: (String) -> Unit,可以看到onValueChange的值类型就是接受一个 string 参数且无返回值的函数
                onValueChange = onUserGuessChanged,
                label = { Text(stringResource(R.string.enter_your_word)) },
                isError = false,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    // 这里是点击键盘的 done 键后执行的函数,这里接受的是 {},所以不执行任何操作,会在后续中添加
                    // Tips: val onDone: (KeyboardActionScope.() -> Unit)? = null,这是 onDone的回调,是一个无参无返回值的函数类型
                    onDone = { onKeyboardDone() }
                )
            )
        }
    }
}

/*
 * Creates and shows an AlertDialog with final score.
 */
@Composable
private fun FinalScoreDialog(
    score: Int,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = (LocalContext.current as Activity)

    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onCloseRequest.
        },
        title = { Text(text = stringResource(R.string.congratulations)) },
        text = { Text(text = stringResource(R.string.you_scored, score)) },
        modifier = modifier,
        dismissButton = {
            TextButton(
                onClick = {
                    activity.finish()
                }
            ) {
                Text(text = stringResource(R.string.exit))
            }
        },
        confirmButton = {
            TextButton(onClick = onPlayAgain) {
                Text(text = stringResource(R.string.play_again))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    UnscrambleTheme {
        GameScreen()
    }
}
