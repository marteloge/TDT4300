build:
	pdflatex main.tex
	bibtex main.aux
	pdflatex main.tex
	pdflatex main.tex

open: build
	open main.pdf