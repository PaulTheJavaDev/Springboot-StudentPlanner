let feedbackElement;

export const setFeedbackLabel = (feedbackElementParameter) => {
    feedbackElement = feedbackElementParameter;
}

export const showFeedback = (message) => {

  feedbackElement.textContent = message;

  setTimeout(() => {
    feedbackElement.textContent = "";
  }, 2000);

};