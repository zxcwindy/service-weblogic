(require 'websocket)
(defvar wstest-ws nil)
(defvar  wstest-ws1 nil)

(defun conn-create ()
  (when (websocket-openp wstest-ws)
    (websocket-close wstest-ws))
  (setf wstest-ws
	(websocket-open
	 "ws://localhost:8080/service/rest/shell/create"
	 :on-message (lambda (websocket frame)
		       (message "ws frame: %S" (decode-coding-string (websocket-frame-payload frame) 'utf-8)))
	 :on-close (lambda (websocket) (message "Websocket closed")))))

(defun conn-send ()
  (when (websocket-openp wstest-ws1)
    (websocket-close wstest-ws1))
  (setf wstest-ws1
	(websocket-open
	 "ws://localhost:8080/service/rest/shell/send"
	 :on-message (lambda (websocket frame)
		       (let ((replay-message (decode-coding-string (websocket-frame-payload frame) 'utf-8)))
			 (if (s-suffix? " " replay-message)
			     (comint-output-filter (get-buffer-process (current-buffer))
						   (concat (substring replay-message 0 (- (length replay-message) 1))
							   "\nshell> "))
			   (insert replay-message))))
	 :on-close (lambda (websocket) (message "Websocket closed")))))

(conn-create)
(conn-send)
(websocket-send-text wstest-ws "test")
(websocket-send-text wstest-ws1 "{\"channelName\":\"test\", \"content\":\"ls -alh ~/zxc\"}")

(define-derived-mode shell-repl-mode comint-mode "shell-REPL"
  "Provide a REPL into the visiting browser."
  (setq comint-prompt-regexp (concat "^" (regexp-quote "shell> "))
        comint-input-sender 'shell-input-sender
        comint-process-echoes nil
	comint-use-prompt-regexp t
	comint-prompt-read-only t)
  (unless (comint-check-proc (current-buffer))
    (insert "welcome\n")
    (start-process "shell-repl" (current-buffer) nil)
    (set-process-query-on-exit-flag (get-buffer-process (current-buffer)) nil)
    (goto-char (point-max))
    (set (make-local-variable 'comint-inhibit-carriage-motion) t)
    (comint-output-filter (get-buffer-process (current-buffer))  "shell> ")
    ;; (comint-output-filter (get-buffer-process (current-buffer)) "\nshell> ")
    ;; (set-process-filter (get-buffer-process (current-buffer)) 'comint-output-filter)
    ))

(defun shell-input-sender (_ input)
  "REPL comint handler."
  (websocket-send-text wstest-ws1 (format "{\"channelName\":\"test\", \"content\":\"%s\"}" input)))


;; (defun shell-result-callback (result)
;;   (with-current-buffer (current-buffer)
;;     (concat "\n" "shell> ")
;;     ;; (comint-output-filter (get-buffer-process (current-buffer))
;;     ;; 			    )
;;     ))

;; comint-output-filter-functions


(defun zxc-shell-comint-output-filter (process string)
  (let ((oprocbuf (process-buffer process)))
    ;; First check for killed buffer or no input.
    (when (and string oprocbuf (buffer-name oprocbuf))
      (with-current-buffer oprocbuf
	;; Run preoutput filters
	(let ((functions comint-preoutput-filter-functions))
	  (while (and functions string)
	    (if (eq (car functions) t)
		(let ((functions
                       (default-value 'comint-preoutput-filter-functions)))
		  (while (and functions string)
		    (setq string (funcall (car functions) string))
		    (setq functions (cdr functions))))
	      (setq string (funcall (car functions) string)))
	    (setq functions (cdr functions))))

	;; Insert STRING
	(let ((inhibit-read-only t)
              ;; The point should float after any insertion we do.
	      (saved-point (copy-marker (point) t)))

	  ;; We temporarily remove any buffer narrowing, in case the
	  ;; process mark is outside of the restriction
	  (save-restriction
	    (widen)

	    (goto-char (- (point-max) 7))
	    (set-marker comint-last-output-start (point))

            ;; Try to skip repeated prompts, which can occur as a result of
            ;; commands sent without inserting them in the buffer.
            (let ((bol (save-excursion (forward-line 0) (point)))) ;No fields.
              (when (and (not (bolp))
                         (looking-back comint-prompt-regexp bol))
                (let* ((prompt (buffer-substring bol (point)))
                       (prompt-re (concat "\\`" (regexp-quote prompt))))
                  (while (string-match prompt-re string)
                    (setq string (substring string (match-end 0)))))))
            (while (string-match (concat "\\(^" comint-prompt-regexp
                                         "\\)\\1+")
                                 string)
              (setq string (replace-match "\\1" nil nil string)))

	    ;; insert-before-markers is a bad thing. XXX
	    ;; Luckily we don't have to use it any more, we use
	    ;; window-point-insertion-type instead.
	    (insert string)

	    ;; Advance process-mark
	    (set-marker (- (point-max) 7) (point))

	    (unless comint-inhibit-carriage-motion
	      ;; Interpret any carriage motion characters (newline, backspace)
	      (comint-carriage-motion comint-last-output-start (point)))

	    ;; Run these hooks with point where the user had it.
	    (goto-char saved-point)
	    (run-hook-with-args 'comint-output-filter-functions string)
	    (set-marker saved-point (point))

	    (goto-char (- (point-max) 7))		; In case a filter moved it.

	    (unless comint-use-prompt-regexp
              (with-silent-modifications
                (add-text-properties comint-last-output-start (point)
                                     '(front-sticky
				       (field inhibit-line-move-field-capture)
				       rear-nonsticky t
				       field output
				       inhibit-line-move-field-capture t))))

	    ;; Highlight the prompt, where we define `prompt' to mean
	    ;; the most recent output that doesn't end with a newline.
	    (let ((prompt-start (save-excursion (forward-line 0) (point)))
		  (inhibit-read-only t))
	      (when comint-prompt-read-only
		(with-silent-modifications
		  (or (= (point-min) prompt-start)
		      (get-text-property (1- prompt-start) 'read-only)
		      (put-text-property (1- prompt-start)
					 prompt-start 'read-only 'fence))
		  (add-text-properties prompt-start (point)
				       '(read-only t front-sticky (read-only)))))
	      (when comint-last-prompt
		(remove-text-properties (car comint-last-prompt)
					(cdr comint-last-prompt)
					'(font-lock-face)))
	      (setq comint-last-prompt
		    (cons (copy-marker prompt-start) (point-marker)))
	      (add-text-properties prompt-start (point)
				   '(rear-nonsticky t
				     font-lock-face comint-highlight-prompt)))
	    (goto-char saved-point)))))))


(defun zxc-comint-output-filter (process string)
  (let ((oprocbuf (process-buffer process)))
    ;; First check for killed buffer or no input.
    (when (and string oprocbuf (buffer-name oprocbuf))
      (with-current-buffer oprocbuf
	;; Run preoutput filters
	(let ((functions comint-preoutput-filter-functions))
	  (while (and functions string)
	    (if (eq (car functions) t)
		(let ((functions
                       (default-value 'comint-preoutput-filter-functions)))
		  (while (and functions string)
		    (setq string (funcall (car functions) string))
		    (setq functions (cdr functions))))
	      (setq string (funcall (car functions) string)))
	    (setq functions (cdr functions))))

	;; Insert STRING
	(let ((inhibit-read-only t)
              ;; The point should float after any insertion we do.
	      (saved-point (copy-marker (point) t)))

	  ;; We temporarily remove any buffer narrowing, in case the
	  ;; process mark is outside of the restriction
	  (save-restriction
	    (widen)

	    (goto-char (process-mark process))
	    (set-marker comint-last-output-start (point))

            ;; Try to skip repeated prompts, which can occur as a result of
            ;; commands sent without inserting them in the buffer.
            (let ((bol (save-excursion (forward-line 0) (point)))) ;No fields.
              (when (and (not (bolp))
                         (looking-back comint-prompt-regexp bol))
                (let* ((prompt (buffer-substring bol (point)))
                       (prompt-re (concat "\\`" (regexp-quote prompt))))
                  (while (string-match prompt-re string)
                    (setq string (substring string (match-end 0)))))))
            (while (string-match (concat "\\(^" comint-prompt-regexp
                                         "\\)\\1+")
                                 string)
              (setq string (replace-match "\\1" nil nil string)))

	    ;; insert-before-markers is a bad thing. XXX
	    ;; Luckily we don't have to use it any more, we use
	    ;; window-point-insertion-type instead.
	    (insert (concat "\n" string))

	    ;; Advance process-mark
	    (set-marker (process-mark process) (point))

	    (unless comint-inhibit-carriage-motion
	      ;; Interpret any carriage motion characters (newline, backspace)
	      (comint-carriage-motion comint-last-output-start (point)))

	    ;; Run these hooks with point where the user had it.
	    (goto-char saved-point)
	    (run-hook-with-args 'comint-output-filter-functions string)
	    (set-marker saved-point (point))

	    (goto-char (process-mark process)) ; In case a filter moved it.

	    (unless comint-use-prompt-regexp
              (with-silent-modifications
                (add-text-properties comint-last-output-start (point)
                                     '(front-sticky
				       (field inhibit-line-move-field-capture)
				       rear-nonsticky t
				       field output
				       inhibit-line-move-field-capture t))))

	    ;; Highlight the prompt, where we define `prompt' to mean
	    ;; the most recent output that doesn't end with a newline.
	    (let ((prompt-start (save-excursion (forward-line 0) (point)))
		  (inhibit-read-only t))
	      (when comint-prompt-read-only
		(with-silent-modifications
		  (or (= (point-min) prompt-start)
		      (get-text-property (1- prompt-start) 'read-only)
		      (put-text-property (1- prompt-start)
					 prompt-start 'read-only 'fence))
		  (add-text-properties prompt-start (point)
				       '(read-only t front-sticky (read-only)))))
	      (when comint-last-prompt
		(remove-text-properties (car comint-last-prompt)
					(cdr comint-last-prompt)
					'(font-lock-face)))
	      (setq comint-last-prompt
		    (cons (copy-marker prompt-start) (point-marker)))
	      (add-text-properties prompt-start (point)
				   '(rear-nonsticky t
				     font-lock-face comint-highlight-prompt)))
	    (goto-char saved-point)))))))
