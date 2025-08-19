/**
 * Charts
 * @requires https://www.chartjs.org/
 */

export default (() => {
    const charts = document.querySelectorAll('[data-chart]')

    if (charts.length === 0) return

    charts.forEach((chartElement) => {
        const {
            chart: chartData
        } = chartElement.dataset
        const options = JSON.parse(chartData)

        if (typeof Chart !== 'undefined') {
            new Chart(chartElement, options) // eslint-disable-line no-undef
        } else {
            console.error('Chart is not defined.')
        }
    })
})()