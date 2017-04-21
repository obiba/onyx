(function() {
  var timeout;

  /**
   * Hide/display the canvas and enable/disable the stop and start buttons
   * @param display
   */
  function displayCanvas(display) {
    var canvas = document.getElementById("canvas");
    canvas.style.visibility =  display ? 'visible' : 'hidden';

    var startRecordBtn = document.getElementById("startRecordBtn");
    startRecordBtn.disabled = display;
    var stopRecordBtn = document.getElementById("stopRecordBtn");
    stopRecordBtn.disabled = !display;
  }

  /**
   * Post the blob to server
   * @param blob
   */
  function doWicketFileUploadCall(blob) {
    var request = new XMLHttpRequest();

    request.open("POST", "${callbackUrl}", true);
    request.setRequestHeader("Wicket-Ajax", "true");
    request.setRequestHeader("Accept", "text/xml");

    request.onload = function (event) {
      console.info("blob uploaded successfully", event);
    };

    request.send(blob);
  }

  /**
   * Utility function to create a blob url and set it to the "src" of the audio element
   * @param blob
   */
  function moveBlobToAudioSrc(blob) {
    var url = window.URL || window.webkitURL;
    var audioElement = document.querySelector('audio');
    audioElement.src = url ? url.createObjectURL(blob) : blob;
  }

  /**
   * Get the blob from server
   */
  function getInitialAudioSrc() {
    var request = new XMLHttpRequest();

    request.onreadystatechange = function () {
      if (request.readyState === XMLHttpRequest.DONE && request.status === 200) {
        moveBlobToAudioSrc(request.response);
      }
    };

    request.open("GET", "${initialSrc}&_=" + Date.now(), true);
    request.responseType = "blob";
    request.setRequestHeader("Wicket-Ajax", "true");

    request.send()
  }

  /**
   * Callback when user media can successfully take control of the stream
   * @param stream
   */
  function record(stream) {
    var mediaRecorder = new MediaRecorder(stream);
    var chunks;

    displayCanvas(false);
    visualize(stream);

    mediaRecorder.onstart = function () {
      chunks = [];
    };

    mediaRecorder.ondataavailable = function(event) {
      chunks.push(event.data);
    };

    mediaRecorder.onstop = function() {
      var blob = new Blob(chunks, { "type" : "audio/webm;codecs=opus" }); // mention the default container and codecs used by supported browsers

      moveBlobToAudioSrc(blob);
      doWicketFileUploadCall(blob);
    };

    /**
     * Button start record event listener
     * Creates a timeout id to stop the recording
     */
    function onButtonStart() {
      timeout = setTimeout(function () {
        onButtonStop();
      }, parseInt("${maxDuration}000", 10)); // assume maxDuration is in seconds

      mediaRecorder.start();
      displayCanvas(true);
    }

    /**
     * Button stop record event listener
     * Clears the timeout
     */
    function onButtonStop() {
      clearTimeout(timeout);

      mediaRecorder.stop();
      displayCanvas(false);
    }

    document.getElementById("startRecordBtn").onclick = onButtonStart;
    document.getElementById("stopRecordBtn").onclick = onButtonStop;
  }

  function rejected(error) {
    console.error(error);
  }

  /**
   * Utility to add "audio" visualisation to the canvas
   * @param stream
   */
  function visualize(stream) {
    var canvas = document.querySelector("canvas");
    var audioContext = new (window.AudioContext || webkitAudioContext)();
    var canvasContext = canvas.getContext("2d");

    var source = audioContext.createMediaStreamSource(stream);
    var analyser = audioContext.createAnalyser();
    analyser.fftSize = 2048;

    var bufferLength = analyser.frequencyBinCount;
    var dataArray = new Uint8Array(bufferLength);

    source.connect(analyser);

    WIDTH = canvas.width;
    HEIGHT = canvas.height;

    draw();

    /**
     * Draw "audio" the visualisation on the canvas
     */
    function draw() {
      requestAnimationFrame(draw);
      analyser.getByteTimeDomainData(dataArray);
      canvasContext.fillStyle = 'rgb(255, 255, 255)';
      canvasContext.fillRect(0, 0, WIDTH, HEIGHT);

      canvasContext.lineWidth = 2;
      canvasContext.strokeStyle = 'rgb(90, 90, 90)';

      canvasContext.beginPath();

      var sliceWidth = WIDTH * 1.0 / bufferLength;
      var x = 0;

      for(var i = 0; i < bufferLength; i++) {
        var v = dataArray[i] / 128.0;
        var y = v * HEIGHT / 2;

        if(i === 0) {
          canvasContext.moveTo(x, y);
        } else {
          canvasContext.lineTo(x, y);
        }

        x += sliceWidth;
      }

      canvasContext.lineTo(canvas.width, canvas.height / 2);
      canvasContext.stroke();
    }
  }

  getInitialAudioSrc();
  navigator.mediaDevices.getUserMedia({audio: true}).then(record, rejected);
})();