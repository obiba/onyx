var audio_context;
var recorder;

function startUserMedia(stream) {
  var input = audio_context.createMediaStreamSource(stream);
  console.log('Media stream created.');
  recorder = new Recorder(input, {'workerPath': 'resources/org.obiba.onyx.wicket.data.DataField/recorderWorker.js'});
  console.log('Recorder initialised.');
}

function startRecording(event, button) {
  recorder || initRecording();
  recorder && recorder.record();
  //button.disabled = true;
  //button.nextElementSibling.disabled = false;
  console.log('Recording...');
  event.stopPropagation();
}

function stopRecording(event, button) {
  recorder && recorder.stop();
  //button.disabled = true;
  //button.previousElementSibling.disabled = false;
  console.log('Stopped recording.');
  event.stopPropagation();

  // create WAV download link using audio data blob
  createDownloadLink();

  recorder.clear();
  recorder = undefined;
  //window.AudioContext.close();
  audio_context = undefined;
}

function createDownloadLink() {
  recorder && recorder.exportWAV(function(blob) {
    var url = URL.createObjectURL(blob);
    var li = document.createElement('li');
    var au = document.createElement('audio');
    var hf = document.createElement('a');

    au.controls = true;
    au.src = url;
    hf.href = url;
    hf.download = new Date().toISOString() + '.wav';
    hf.innerHTML = hf.download;
    li.appendChild(au);
    li.appendChild(hf);
    recordingslist.appendChild(li);
  });
}

function initRecording() {
  try {
    // webkit shim
    window.AudioContext = window.AudioContext || window.webkitAudioContext;
    navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
    window.URL = window.URL || window.webkitURL;

    audio_context = new AudioContext;
    console.log('Audio context set up.');
    console.log('navigator.getUserMedia ' + (navigator.getUserMedia ? 'available.' : 'not present!'));
  } catch (e) {
    alert('No web audio support in this browser!');
  }

  navigator.getUserMedia({audio: true}, startUserMedia, function(e) {
    console.log('No live audio input: ' + e);
  });
}