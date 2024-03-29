package org.redbyte.genom.render.opengl

import android.opengl.GLSurfaceView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.redbyte.genom.common.GameBoard
import org.redbyte.genom.common.data.GameSettings
import org.redbyte.genom.settings.GameSettingsViewModel

@Composable
fun Genom2DGame() {
    val viewModel: GameSettingsViewModel = viewModel()
    val gameBoard = viewModel.getGameBoard()
    val livingCellsCount = remember { mutableIntStateOf(gameBoard.settings.initialPopulation) }
    val turnGame = remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { context ->
                GLSurfaceView(context).apply {
                    setEGLContextClientVersion(2)
                    setRenderer(GameRenderer(gameBoard) { count, turn ->
                        livingCellsCount.intValue = count
                        turnGame.intValue = turn
                    })
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                }
            }
        )

        Text(
            text = "Количество живых клеток: ${livingCellsCount.intValue}\n" +
                    "Ход: ${turnGame.intValue}",
            color = Color(0xFF009688),
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp, start = 16.dp)
        )

    }
}
