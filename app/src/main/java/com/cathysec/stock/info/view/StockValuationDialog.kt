package com.cathysec.stock.info.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.cathysec.stock.info.R
import com.cathysec.stock.info.model.LoadError
import com.cathysec.stock.info.theme.StockTheme
import com.cathysec.stock.info.util.LoadErrorUtil
import com.cathysec.stock.info.viewmodel.ValuationDialogUiState

/**
 * Dialog with the P/E ratio, dividend yield and P/B ratio of one stock.
 *
 * @param state what to show: the values, a loading message, "no data" or an error with a retry button
 * @param onRetry called when the user taps the retry button of the error state
 * @param onDismiss called when the user taps outside of the dialog or presses back
 */
@Composable
fun StockValuationDialog(
    state: ValuationDialogUiState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            // Code on the first line, name on the second line; each stays on one line.
            Column {
                Text(
                    text = state.code,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        text = { StockValuationContent(state = state, onRetry = onRetry) },
        // No button: the dialog closes by tapping outside of it.
        confirmButton = {},
    )
}

@Composable
private fun StockValuationContent(
    state: ValuationDialogUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is ValuationDialogUiState.Loaded -> Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.valuation_row_spacing)),
        ) {
            ValuationRow(stringResource(R.string.label_pe_ratio), state.peRatio)
            ValuationRow(stringResource(R.string.label_dividend_yield), state.dividendYield)
            ValuationRow(stringResource(R.string.label_pb_ratio), state.pbRatio)
        }

        is ValuationDialogUiState.Loading -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.valuation_progress_size)))
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.valuation_loading_gap)))
            Text(
                text = stringResource(R.string.valuation_loading),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        is ValuationDialogUiState.NoData -> Text(
            text = stringResource(R.string.valuation_no_data),
            modifier = modifier,
            style = MaterialTheme.typography.bodyLarge,
        )

        is ValuationDialogUiState.Failed -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.valuation_row_spacing)),
        ) {
            Text(
                text = stringResource(LoadErrorUtil.messageRes(state.error)),
                style = MaterialTheme.typography.bodyLarge,
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
        }
    }
}

@Composable
private fun ValuationRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun StockValuationDialogLoadedPreview() {
    StockTheme {
        StockValuationDialog(
            state = ValuationDialogUiState.Loaded("2330", "TSMC", "26.20", "1.50%", "6.10"),
            onRetry = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StockValuationDialogFailedPreview() {
    StockTheme {
        StockValuationDialog(
            state = ValuationDialogUiState.Failed("2330", "TSMC", LoadError.NO_CONNECTION),
            onRetry = {},
            onDismiss = {},
        )
    }
}
