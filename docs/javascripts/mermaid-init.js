// Mermaid diagram initialization for Kafka Training documentation

document.addEventListener('DOMContentLoaded', function() {
  mermaid.initialize({
    startOnLoad: true,
    theme: document.body.getAttribute('data-md-color-scheme') === 'slate' ? 'dark' : 'default',
    securityLevel: 'loose',
    fontFamily: 'Work Sans, sans-serif',
    flowchart: {
      useMaxWidth: true,
      htmlLabels: true,
      curve: 'basis',
      padding: 15
    },
    sequence: {
      useMaxWidth: true,
      diagramMarginX: 50,
      diagramMarginY: 10,
      actorMargin: 50,
      width: 150,
      height: 65,
      boxMargin: 10,
      boxTextMargin: 5,
      noteMargin: 10,
      messageMargin: 35
    },
    gantt: {
      useMaxWidth: true,
      titleTopMargin: 25,
      barHeight: 20,
      barGap: 4,
      topPadding: 50,
      leftPadding: 75,
      gridLineStartPadding: 35,
      fontSize: 11,
      numberSectionStyles: 4,
      axisFormat: '%Y-%m-%d'
    }
  });

  // Re-initialize Mermaid when theme changes
  var observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
      if (mutation.attributeName === 'data-md-color-scheme') {
        var theme = document.body.getAttribute('data-md-color-scheme') === 'slate' ? 'dark' : 'default';
        mermaid.initialize({ theme: theme });

        // Re-render all diagrams
        document.querySelectorAll('.mermaid').forEach(function(element) {
          var graphDefinition = element.textContent;
          element.removeAttribute('data-processed');
          element.innerHTML = graphDefinition;
        });

        mermaid.init(undefined, document.querySelectorAll('.mermaid'));
      }
    });
  });

  observer.observe(document.body, {
    attributes: true,
    attributeFilter: ['data-md-color-scheme']
  });
});
